"""
Reference-image provider for the Capture Judge.

For a given task we need a "ground-truth" photo of the place/subject so the
vision model can compare the user's upload against it. We fetch that reference
once via SerpAPI (Google Images), cache it on disk keyed by task id, and reuse
it on every later verification — so each task costs at most one search.

If no SerpAPI key is configured (or the search/download fails) the caller gets
None and falls back to text-only judging.
"""

from __future__ import annotations

import logging
import re
from pathlib import Path

import httpx

from services.ai.common.config import settings

logger = logging.getLogger(__name__)

_SERPAPI_URL = "https://serpapi.com/search.json"
_UA = "Mozilla/5.0 (BeVietnam CaptureJudge reference fetcher)"
_MAX_BYTES = 6_000_000  # skip absurdly large originals


def _repo_root() -> Path:
    # reference.py → capture_judge → agents → ai → services → repo root
    return Path(__file__).resolve().parents[4]


def _cache_dir() -> Path:
    base = Path(settings.reference_cache_dir)
    path = base if base.is_absolute() else _repo_root() / base
    path.mkdir(parents=True, exist_ok=True)
    return path


def _safe_key(task_id: str) -> str:
    key = re.sub(r"[^a-zA-Z0-9_-]+", "-", task_id).strip("-")
    return key or "unknown"


def _cached_path(task_id: str) -> Path | None:
    folder = _cache_dir()
    for p in folder.glob(f"{_safe_key(task_id)}.*"):
        if p.is_file() and p.stat().st_size > 0:
            return p
    return None


def _mime_for(path: Path) -> str:
    ext = path.suffix.lower().lstrip(".")
    if ext in {"jpg", "jpeg"}:
        return "image/jpeg"
    if ext == "png":
        return "image/png"
    if ext == "webp":
        return "image/webp"
    return "image/jpeg"


def _search_image_url(query: str) -> str | None:
    """Top Google-Images result URL for the query, via SerpAPI."""
    if not settings.serpapi_api_key:
        return None
    try:
        resp = httpx.get(
            _SERPAPI_URL,
            params={
                "engine": "google_images",
                "q": query,
                "api_key": settings.serpapi_api_key,
                "num": "10",
                "ijn": "0",
                "safe": "active",
            },
            timeout=20.0,
        )
        resp.raise_for_status()
        results = resp.json().get("images_results") or []
    except (httpx.HTTPError, ValueError) as exc:
        logger.warning("SerpAPI image search failed for %r: %s", query, exc)
        return None

    for item in results:
        url = item.get("original") or item.get("thumbnail")
        if isinstance(url, str) and url.startswith(("http://", "https://")):
            return url
    return None


def _download(url: str) -> tuple[bytes, str] | None:
    try:
        with httpx.stream(
            "GET", url, timeout=20.0, follow_redirects=True, headers={"User-Agent": _UA}
        ) as resp:
            resp.raise_for_status()
            content_type = resp.headers.get("content-type", "")
            if not content_type.startswith("image/"):
                return None
            data = resp.read()
    except httpx.HTTPError as exc:
        logger.warning("Reference image download failed (%s): %s", url, exc)
        return None

    if not data or len(data) > _MAX_BYTES:
        return None
    ext = {"image/png": "png", "image/webp": "webp"}.get(content_type.split(";")[0], "jpg")
    return data, ext


def get_reference_image(task_id: str, query: str) -> tuple[bytes, str] | None:
    """
    Return (image_bytes, mime_type) for the task's reference photo.

    Uses the on-disk cache first; otherwise searches SerpAPI, downloads the top
    result, caches it, and returns it. Returns None if unavailable.
    """
    cached = _cached_path(task_id)
    if cached:
        return cached.read_bytes(), _mime_for(cached)

    url = _search_image_url(query)
    if not url:
        return None

    downloaded = _download(url)
    if not downloaded:
        return None

    data, ext = downloaded
    try:
        out = _cache_dir() / f"{_safe_key(task_id)}.{ext}"
        out.write_bytes(data)
    except OSError as exc:
        logger.warning("Could not cache reference image for %s: %s", task_id, exc)

    return data, {"png": "image/png", "webp": "image/webp"}.get(ext, "image/jpeg")
