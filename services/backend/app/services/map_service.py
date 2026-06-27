"""Goong MapTiles adapter for backend-hosted MapLibre styles."""

from __future__ import annotations

import copy
import logging
import time
from typing import Any
from urllib.parse import parse_qs, quote, urlencode, urlparse, urlunparse

import httpx

from services.backend.app.core.config import settings

logger = logging.getLogger(__name__)

_ALLOWED_GOONG_HOST = "tiles.goong.io"


class GoongMapService:
    def __init__(self, ttl_seconds: int = 600) -> None:
        self._ttl_seconds = ttl_seconds
        self._style_cache: tuple[dict[str, Any], float] | None = None

    @property
    def enabled(self) -> bool:
        return bool(self._api_key)

    @property
    def _api_key(self) -> str:
        # Prioritize server-side GOONG_API_KEY for all backend requests to avoid domain/referer restriction issues
        return settings.GOONG_API_KEY or settings.GOONG_MAPTILES_KEY

    def backend_style_url(self) -> str:
        return f"{settings.PUBLIC_API_BASE_URL.rstrip('/')}/maps/style"

    async def get_style(self) -> dict[str, Any]:
        if not self.enabled:
            raise RuntimeError("Goong MapTiles key is not configured")

        cached = self._style_cache
        if cached:
            style, cached_at = cached
            if time.monotonic() - cached_at < self._ttl_seconds:
                return copy.deepcopy(style)

        style_url = self._with_api_key(settings.GOONG_MAP_STYLE_URL)
        async with httpx.AsyncClient(timeout=settings.GOONG_TIMEOUT) as client:
            response = await client.get(style_url)
            response.raise_for_status()
            payload = response.json()

        if not isinstance(payload, dict):
            raise ValueError("Goong style response is not an object")

        rewritten = self._rewrite_style(payload)
        self._style_cache = (rewritten, time.monotonic())
        return copy.deepcopy(rewritten)

    async def proxy_url(self, target_url: str) -> tuple[bytes, str]:
        if not self.enabled:
            raise RuntimeError("Goong MapTiles key is not configured")
        if not self._is_allowed_goong_url(target_url):
            raise ValueError("Unsupported map asset URL")

        url = self._with_api_key(target_url)
        async with httpx.AsyncClient(timeout=settings.GOONG_TIMEOUT) as client:
            response = await client.get(url)
            response.raise_for_status()
            content_type = response.headers.get("content-type", "application/octet-stream")
            return response.content, content_type

    def _rewrite_style(self, style: dict[str, Any]) -> dict[str, Any]:
        rewritten = copy.deepcopy(style)
        self._rewrite_value(rewritten)
        return rewritten

    def _rewrite_value(self, value: Any) -> Any:
        if isinstance(value, dict):
            for key, child in list(value.items()):
                if isinstance(child, str):
                    value[key] = self._rewrite_string(child)
                else:
                    self._rewrite_value(child)
        elif isinstance(value, list):
            for index, child in enumerate(value):
                if isinstance(child, str):
                    value[index] = self._rewrite_string(child)
                else:
                    self._rewrite_value(child)
        return value

    def _rewrite_string(self, value: str) -> str:
        if value.startswith("https://"):
            if self._is_allowed_goong_url(value):
                return self._proxy_url(value)
            return value
        if value.startswith("//"):
            normalized = f"https:{value}"
            if self._is_allowed_goong_url(normalized):
                return self._proxy_url(normalized)
            return value
        if value.startswith("/"):
            return self._proxy_url(f"https://{_ALLOWED_GOONG_HOST}{value}")
        return value

    def _proxy_url(self, target_url: str) -> str:
        base = f"{settings.PUBLIC_API_BASE_URL.rstrip('/')}/maps/proxy"
        safe_target = self._without_api_key(target_url)
        return f"{base}?url={quote(safe_target, safe='{}')}"

    def _without_api_key(self, raw_url: str) -> str:
        parsed = urlparse(raw_url)
        query = parse_qs(parsed.query, keep_blank_values=True)
        query.pop("api_key", None)
        return urlunparse(parsed._replace(query=urlencode(query, doseq=True)))

    def _with_api_key(self, raw_url: str) -> str:
        parsed = urlparse(raw_url)
        query = parse_qs(parsed.query, keep_blank_values=True)
        query["api_key"] = [self._api_key]
        return urlunparse(parsed._replace(query=urlencode(query, doseq=True)))

    def _is_allowed_goong_url(self, raw_url: str) -> bool:
        parsed = urlparse(raw_url)
        return parsed.scheme == "https" and parsed.netloc == _ALLOWED_GOONG_HOST


map_service = GoongMapService()
