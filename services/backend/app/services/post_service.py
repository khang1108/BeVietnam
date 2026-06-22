"""Spotlight post loading + rule-based personalized ranking for the feed.

Posts are pre-built offline (data/posts/*.json). Ranking is deterministic:
score = interest match + time/weather/season fit, with a novelty penalty for
places the user has already visited. No LLM call at request time.
"""

from __future__ import annotations

import json
import logging
from datetime import datetime, timezone
from pathlib import Path
from typing import Iterable

from services.backend.app.core.config import settings
from services.backend.app.schemas.feed import FeedItem

logger = logging.getLogger(__name__)

# Raw score ceiling used to normalize into FeedItem's 0.0–1.0 range.
_SCORE_NORM = 60.0


def _repo_root() -> Path:
    # services/backend/app/services/post_service.py -> repo root is 4 levels up.
    return Path(__file__).resolve().parents[4]


def _resolve_path() -> Path:
    configured = Path(settings.SPOTLIGHTS_PATH).expanduser()
    return configured if configured.is_absolute() else _repo_root() / configured


def _normalize_tag(value: str | None) -> str:
    if not value:
        return ""
    return value.strip().lower().replace(" ", "_")


def _infer_time_of_day(now: datetime | None = None) -> str:
    hour = (now or datetime.now()).hour
    if 5 <= hour < 11:
        return "morning"
    if 11 <= hour < 17:
        return "afternoon"
    if 17 <= hour < 21:
        return "evening"
    return "night"


def _infer_season(now: datetime | None = None) -> str:
    month = (now or datetime.now()).month
    if month in (2, 3, 4):
        return "spring"
    if month in (5, 6, 7):
        return "summer"
    if month in (8, 9, 10):
        return "autumn"
    return "winter"


class PostService:
    """Ranks pre-built spotlight posts for a specific user + context."""

    def load_posts(self) -> list[dict]:
        path = _resolve_path()
        if not path.exists():
            logger.warning("Spotlights file not found: %s", path)
            return []
        try:
            payload = json.loads(path.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError) as exc:
            logger.warning("Spotlights load failed from %s: %s", path, exc)
            return []
        posts = payload.get("posts", payload) if isinstance(payload, dict) else payload
        return [p for p in posts if isinstance(p, dict)] if isinstance(posts, list) else []

    def rank(
        self,
        *,
        interests: Iterable[str],
        visited_place_ids: Iterable[str] = (),
        weather: str | None = None,
        time_of_day: str | None = None,
        limit: int = 20,
    ) -> list[FeedItem]:
        interest_tags = {_normalize_tag(t) for t in interests if t}
        visited = set(visited_place_ids)
        now = datetime.now(timezone.utc)
        ctx_weather = _normalize_tag(weather)
        ctx_time = _normalize_tag(time_of_day) or _infer_time_of_day()
        ctx_season = _infer_season()

        items: list[FeedItem] = []
        for post in self.load_posts():
            score = self._score(post, interest_tags, visited, ctx_weather, ctx_time, ctx_season)
            normalized = max(0.0, min(1.0, score / _SCORE_NORM))
            items.append(
                FeedItem(
                    id=str(post.get("post_id") or post.get("place_id") or ""),
                    place_id=str(post.get("place_id") or ""),
                    name=str(post.get("place_name") or post.get("title") or ""),
                    category=(post.get("categories") or ["general"])[0],
                    thumbnail_url=post.get("image_url"),
                    score=round(normalized, 3),
                    explanation=str(
                        post.get("cultural_hook") or post.get("body") or post.get("title") or ""
                    ),
                    created_at=now,
                )
            )

        items.sort(key=lambda i: i.score, reverse=True)
        return items[:limit]

    def _score(
        self,
        post: dict,
        interest_tags: set[str],
        visited: set[str],
        ctx_weather: str,
        ctx_time: str,
        ctx_season: str,
    ) -> float:
        score = 5.0  # base so every post has a small floor

        categories = {_normalize_tag(c) for c in post.get("categories", [])}
        overlap = interest_tags & categories
        if overlap:
            score += 10 + 5 * len(overlap)  # primary per-user signal

        weather_tags = {_normalize_tag(t) for t in post.get("weather_tags", [])}
        if ctx_weather and ctx_weather in weather_tags:
            score += 20
        elif "any" in weather_tags or not weather_tags:
            score += 6

        time_tags = {_normalize_tag(t) for t in post.get("time_tags", [])}
        if ctx_time in time_tags:
            score += 12
        elif "any" in time_tags or not time_tags:
            score += 4

        season_tags = {_normalize_tag(t) for t in post.get("season_tags", [])}
        if ctx_season in season_tags:
            score += 8

        # Novelty: down-rank places the user already visited.
        if post.get("place_id") in visited:
            score -= 25

        return score


post_service = PostService()
