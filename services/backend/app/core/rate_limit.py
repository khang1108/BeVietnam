"""Global, per-process rate limiting for shared third-party free tiers.

All users share one provider quota, so these limiters are process-wide (not
per-user). Each limiter combines a token bucket (smooths bursts + sustained
rate) with a hard daily cap (protects monthly free quota during a demo).

Callers fail SOFT: if `allow()` is False, return your existing fallback instead
of calling the third-party — the demo degrades gracefully, never hangs.
"""

from __future__ import annotations

import asyncio
import logging
import time
from datetime import date

from services.backend.app.core.config import settings

logger = logging.getLogger(__name__)


class _TokenBucket:
    def __init__(self, rate_per_sec: float, capacity: float) -> None:
        self._rate = rate_per_sec
        self._capacity = max(1.0, capacity)
        self._tokens = self._capacity
        self._updated = time.monotonic()

    def try_take(self) -> bool:
        now = time.monotonic()
        self._tokens = min(self._capacity, self._tokens + (now - self._updated) * self._rate)
        self._updated = now
        if self._tokens >= 1.0:
            self._tokens -= 1.0
            return True
        return False


class RateLimiter:
    """Token bucket (per-minute) + optional hard daily cap. Async-safe."""

    def __init__(self, name: str, per_minute: int, daily_cap: int) -> None:
        self.name = name
        self._bucket = _TokenBucket(rate_per_sec=per_minute / 60.0, capacity=per_minute)
        self._daily_cap = daily_cap
        self._day = date.today()
        self._count = 0
        self._lock = asyncio.Lock()

    async def allow(self) -> bool:
        if not settings.RATE_LIMIT_ENABLED:
            return True
        async with self._lock:
            today = date.today()
            if today != self._day:
                self._day = today
                self._count = 0
            if self._daily_cap and self._count >= self._daily_cap:
                logger.warning("[rate-limit] %s daily cap %d reached", self.name, self._daily_cap)
                return False
            if not self._bucket.try_take():
                logger.warning("[rate-limit] %s per-minute limit hit", self.name)
                return False
            self._count += 1
            return True


foursquare_limiter = RateLimiter("foursquare", settings.FOURSQUARE_RPM, settings.FOURSQUARE_DAILY)
openweather_limiter = RateLimiter("openweather", settings.OPENWEATHER_RPM, settings.OPENWEATHER_DAILY)
goong_limiter = RateLimiter("goong", settings.GOONG_RPM, settings.GOONG_DAILY)
