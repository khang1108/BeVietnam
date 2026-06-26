"""
Enrich stored feed spotlight posts with stable MinIO image URLs.

This is an offline script: SerpAPI is used once per missing post image, then the
image is downloaded to MinIO and the returned backend media URL is written into
`data/posts/hue_spotlights.json` as `image_url`. Runtime `/feed` only reads that
stored field; it never searches SerpAPI.

Run from repo root with `.env` configured:
    python scripts/data-pipeline/enrich_feed_images.py --limit 2
    python scripts/data-pipeline/enrich_feed_images.py
"""

from __future__ import annotations

import argparse
import json
import logging
import re
import sys
from pathlib import Path
from typing import Any

import httpx

SCRIPT_DIR = Path(__file__).resolve().parent
REPO_ROOT = SCRIPT_DIR.parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from services.backend.app.core.config import settings  # noqa: E402
from services.backend.app.core.minio_client import minio_client  # noqa: E402

logger = logging.getLogger("enrich_feed_images")

SERPAPI_URL = "https://serpapi.com/search.json"
USER_AGENT = "Mozilla/5.0 (BeVietnam feed image enricher)"
MAX_BYTES = 6_000_000
POSTS_PATH = REPO_ROOT / "data" / "posts" / "hue_spotlights.json"

CONTENT_TYPE_EXT = {
    "image/jpeg": "jpg",
    "image/png": "png",
    "image/webp": "webp",
}


def safe_key(value: str) -> str:
    key = re.sub(r"[^a-zA-Z0-9_-]+", "-", value).strip("-").lower()
    return key or "unknown"


def load_collection(path: Path) -> tuple[dict[str, Any], list[dict[str, Any]]]:
    payload = json.loads(path.read_text(encoding="utf-8"))
    posts = payload.get("posts") if isinstance(payload, dict) else payload
    if not isinstance(payload, dict) or not isinstance(posts, list):
        raise ValueError(f"Unsupported spotlight file shape: {path}")
    return payload, [p for p in posts if isinstance(p, dict)]


def build_query(post: dict[str, Any]) -> str:
    place_name = str(post.get("place_name") or "").strip()
    title = str(post.get("title") or "").strip()
    return " ".join(part for part in [place_name, title, "Huế Việt Nam địa danh"] if part)


def search_image_urls(query: str) -> list[str]:
    resp = httpx.get(
        SERPAPI_URL,
        params={
            "engine": "google_images",
            "q": query,
            "api_key": settings.SERPAPI_API_KEY,
            "num": "10",
            "ijn": "0",
            "safe": "active",
        },
        timeout=20.0,
    )
    resp.raise_for_status()
    results = resp.json().get("images_results") or []
    urls: list[str] = []
    for item in results:
        if not isinstance(item, dict):
            continue
        url = item.get("original") or item.get("thumbnail")
        if isinstance(url, str) and url.startswith(("http://", "https://")):
            urls.append(url)
    return urls


def download_image(url: str) -> tuple[bytes, str] | None:
    try:
        with httpx.stream(
            "GET",
            url,
            timeout=20.0,
            follow_redirects=True,
            headers={"User-Agent": USER_AGENT},
        ) as resp:
            resp.raise_for_status()
            content_type = (resp.headers.get("content-type") or "").split(";")[0]
            if content_type not in CONTENT_TYPE_EXT:
                return None
            data = resp.read()
    except httpx.HTTPError as exc:
        logger.info("download failed: %s (%s)", url, exc)
        return None

    if not data or len(data) > MAX_BYTES:
        return None
    return data, content_type


def find_image(query: str) -> tuple[bytes, str] | None:
    for url in search_image_urls(query):
        downloaded = download_image(url)
        if downloaded:
            return downloaded
    return None


def enrich_post(post: dict[str, Any], dry_run: bool) -> bool:
    post_id = str(post.get("post_id") or post.get("place_id") or "unknown")
    query = build_query(post)

    if dry_run:
        logger.info("dry-run %s -> %s", post_id, query)
        return False

    found = find_image(query)
    if not found:
        logger.warning("no image found for %s", post_id)
        return False

    data, content_type = found
    ext = CONTENT_TYPE_EXT[content_type]
    place_id = str(post.get("place_id") or post_id)
    key = f"{settings.FEED_IMAGE_PREFIX.rstrip('/')}/{safe_key(place_id)}.{ext}"
    image_url = minio_client.upload_bytes(key, data, content_type)
    post["image_url"] = image_url
    logger.info("enriched %s -> %s", post_id, image_url)
    return True


def main() -> int:
    parser = argparse.ArgumentParser(description="Enrich feed spotlight posts with MinIO images.")
    parser.add_argument("--posts", type=Path, default=POSTS_PATH, help="Spotlight JSON path.")
    parser.add_argument("--limit", type=int, default=0, help="Maximum posts to enrich; 0 = all.")
    parser.add_argument("--force", action="store_true", help="Refresh posts that already have image_url.")
    parser.add_argument("--dry-run", action="store_true", help="Print queries without SerpAPI/MinIO writes.")
    args = parser.parse_args()

    logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")

    if not args.posts.exists():
        logger.error("posts file not found: %s", args.posts)
        return 1
    if not args.dry_run and not settings.SERPAPI_API_KEY:
        logger.error("SERPAPI_API_KEY is required for enrichment")
        return 1

    payload, posts = load_collection(args.posts)
    candidates = [p for p in posts if args.force or not p.get("image_url")]
    if args.limit > 0:
        candidates = candidates[: args.limit]

    logger.info("posts=%d candidates=%d", len(posts), len(candidates))
    changed = 0
    for post in candidates:
        try:
            if enrich_post(post, args.dry_run):
                changed += 1
        except (httpx.HTTPError, OSError, ValueError) as exc:
            logger.warning("failed %s: %s", post.get("post_id"), exc)

    if changed:
        args.posts.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    logger.info("changed=%d", changed)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
