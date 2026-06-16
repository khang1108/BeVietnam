"""OpenWeather context resolver with small in-memory TTL cache."""

from __future__ import annotations

import logging
import time
from dataclasses import dataclass
from typing import Any

import httpx

from services.backend.app.core.config import settings

logger = logging.getLogger(__name__)


@dataclass(frozen=True)
class WeatherResult:
    """Normalized weather result used by pool scoring and Explore bubbles."""

    condition: str
    temp: float | None
    source: str


class OpenWeatherResolver:
    """Small OpenWeather REST adapter. Safe no-op until a key is configured."""

    def __init__(self, ttl_seconds: int = 600) -> None:
        self._ttl_seconds = ttl_seconds
        self._cache: dict[tuple[float, float], tuple[WeatherResult, float]] = {}

    async def get_condition(self, lat: float, lng: float) -> WeatherResult:
        if not settings.OPENWEATHER_API_KEY:
            return WeatherResult(condition="any", temp=None, source="disabled")

        key = self._cache_key(lat, lng)
        cached = self._cache.get(key)
        if cached:
            result, cached_at = cached
            if time.monotonic() - cached_at < self._ttl_seconds:
                return WeatherResult(
                    condition=result.condition,
                    temp=result.temp,
                    source="cache",
                )

        try:
            async with httpx.AsyncClient(timeout=settings.OPENWEATHER_TIMEOUT) as client:
                response = await client.get(
                    f"{settings.OPENWEATHER_BASE_URL.rstrip('/')}/weather",
                    params={
                        "lat": lat,
                        "lon": lng,
                        "appid": settings.OPENWEATHER_API_KEY,
                        "units": "metric",
                    },
                )
                response.raise_for_status()
                payload = response.json()
        except (httpx.RequestError, httpx.HTTPStatusError, ValueError) as exc:
            logger.warning("OpenWeather current weather failed: %s", exc)
            return WeatherResult(condition="any", temp=None, source="fallback")

        result = self._parse_weather(payload)
        self._cache[key] = (result, time.monotonic())
        return result

    async def get_conditions_batch(
        self,
        coords: list[tuple[float, float]],
    ) -> list[WeatherResult]:
        return [await self.get_condition(lat, lng) for lat, lng in coords]

    def _cache_key(self, lat: float, lng: float) -> tuple[float, float]:
        return (round(lat, 1), round(lng, 1))

    def _parse_weather(self, payload: dict[str, Any]) -> WeatherResult:
        weather_items = payload.get("weather")
        weather_main = ""
        if isinstance(weather_items, list) and weather_items:
            first = weather_items[0]
            if isinstance(first, dict):
                weather_main = str(first.get("main") or "")

        main = payload.get("main") if isinstance(payload.get("main"), dict) else {}
        temp = _coerce_float(main.get("temp"))
        condition = _map_condition(weather_main, temp)
        return WeatherResult(condition=condition, temp=temp, source="openweather")


def _coerce_float(value: Any) -> float | None:
    try:
        return float(value)
    except (TypeError, ValueError):
        return None


def _map_condition(weather_main: str, temp: float | None) -> str:
    normalized = weather_main.strip().lower()
    if normalized == "clear":
        return "hot" if temp is not None and temp >= 33 else "sunny"
    if normalized == "clouds":
        return "cloudy"
    if normalized in {"rain", "drizzle", "thunderstorm", "snow"}:
        return "rainy"
    if normalized in {"mist", "fog", "haze", "smoke"}:
        return "cloudy"
    return "any"


weather_service = OpenWeatherResolver()
