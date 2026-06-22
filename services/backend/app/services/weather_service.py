"""OpenWeather context resolver with small in-memory TTL cache."""

from __future__ import annotations

import logging
import math
import time
from dataclasses import dataclass, replace
from datetime import datetime, timezone
from typing import Any

import httpx

from services.backend.app.core.config import settings
from services.backend.app.core.rate_limit import openweather_limiter

logger = logging.getLogger(__name__)


@dataclass(frozen=True)
class WeatherResult:
    """Normalized weather result used by pool scoring and Explore bubbles."""

    condition: str
    temp: float | None
    source: str
    uvi: float | None = None  # estimated UV index (no paid One Call API)
    rain_mm: float | None = None  # rain volume last 1h/3h, mm
    clouds: int | None = None  # cloud cover %


def _estimate_uvi(lat: float, lng: float, dt_unix: int | None, clouds_pct: float) -> float:
    """Rough clear-sky UV index from solar elevation, reduced by cloud cover.
    No paid UV API — this is an estimate, good enough for a 'high/low' cue."""
    dt = datetime.fromtimestamp(dt_unix, tz=timezone.utc) if dt_unix else datetime.now(timezone.utc)
    day = dt.timetuple().tm_yday
    decl = math.radians(23.45) * math.sin(math.radians(360 / 365 * (284 + day)))
    solar_hour = dt.hour + dt.minute / 60 + lng / 15.0
    hour_angle = math.radians(15 * (solar_hour - 12))
    lat_r = math.radians(lat)
    sin_elev = math.sin(lat_r) * math.sin(decl) + math.cos(lat_r) * math.cos(decl) * math.cos(hour_angle)
    elev = math.asin(max(-1.0, min(1.0, sin_elev)))
    if elev <= 0:
        return 0.0
    clear_sky = 12.5 * (math.sin(elev) ** 1.5)
    cloud_factor = 1 - 0.6 * (max(0.0, min(100.0, clouds_pct)) / 100.0)
    return round(max(0.0, clear_sky * cloud_factor), 1)


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
                return replace(result, source="cache")

        # Shared free-tier guard. On limit, serve a stale cache entry if we have
        # one, else a neutral fallback — never block the request.
        if not await openweather_limiter.allow():
            if cached:
                return replace(cached[0], source="cache-stale")
            return WeatherResult(condition="any", temp=None, source="rate_limited")

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

        clouds_obj = payload.get("clouds") if isinstance(payload.get("clouds"), dict) else {}
        clouds = clouds_obj.get("all")
        rain_obj = payload.get("rain") if isinstance(payload.get("rain"), dict) else {}
        rain_mm = _coerce_float(rain_obj.get("1h") or rain_obj.get("3h"))
        coord = payload.get("coord") if isinstance(payload.get("coord"), dict) else {}
        lat = _coerce_float(coord.get("lat")) or 0.0
        lng = _coerce_float(coord.get("lon")) or 0.0
        dt_unix = payload.get("dt") if isinstance(payload.get("dt"), int) else None
        uvi = _estimate_uvi(lat, lng, dt_unix, float(clouds or 0))

        return WeatherResult(
            condition=condition,
            temp=temp,
            source="openweather",
            uvi=uvi,
            rain_mm=rain_mm,
            clouds=int(clouds) if isinstance(clouds, (int, float)) else None,
        )


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
