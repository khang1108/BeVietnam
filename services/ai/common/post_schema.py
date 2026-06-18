"""
Spotlight post schema for the BeVietnam recommendation feed.

A SpotlightPost is an authored, reviewed "place spotlight" — the feed's stored
content type (distinct from the runtime score). The feed surfaces a post when
the current weather/time/season match its tags, then ranks it by the Trip
Advisor suitability score.

Every post stays grounded: its claims trace to >=1 SourceRef (reused from the
knowledge schema), and book/auto-drafted posts carry review_status=needs_review
until a human approves them — same policy as KnowledgeChunk.
"""

from __future__ import annotations

import re
from datetime import date

from pydantic import BaseModel, Field, field_validator, model_validator

from services.ai.common.knowledge_schema import ReviewStatus, SourceRef

_SLUG_PATTERN = re.compile(r"^[a-z0-9]+(?:-[a-z0-9]+)*$")
_SUPPORTED_LANGUAGES = {"vi", "en"}

# Controlled condition vocab — the feed matches these against live context.
# Empty tag list = no constraint on that axis (post is always eligible there).
WEATHER_TAGS = {"sunny", "cloudy", "rainy", "hot", "cool"}
TIME_TAGS = {"morning", "midday", "evening", "night"}
SEASON_TAGS = {"dry", "wet"}  # Huế: dry ~Mar–Aug, wet ~Sep–Feb


class SpotlightPost(BaseModel):
    """An authored, condition-tagged feed post for one place."""

    post_id: str = Field(min_length=3, max_length=120)
    place_id: str = Field(min_length=1, max_length=120)
    place_name: str = Field(min_length=1, max_length=160)
    title: str = Field(min_length=4, max_length=160)
    body: str = Field(min_length=40, max_length=900)
    cultural_hook: str = Field(default="", max_length=400)
    language: str = Field(default="vi", min_length=2, max_length=5)

    weather_tags: list[str] = Field(default_factory=list)
    time_tags: list[str] = Field(default_factory=list)
    season_tags: list[str] = Field(default_factory=list)
    categories: list[str] = Field(default_factory=list)

    source_refs: list[SourceRef] = Field(min_length=1)
    review_status: ReviewStatus = ReviewStatus.NEEDS_REVIEW
    reviewed_at: date
    tags: list[str] = Field(default_factory=list)
    notes: str = Field(default="", max_length=500)

    @field_validator("post_id", "place_id")
    @classmethod
    def validate_slug(cls, value: str) -> str:
        normalized = value.strip().lower()
        if not _SLUG_PATTERN.fullmatch(normalized):
            raise ValueError("must be a lowercase slug of letters, numbers, hyphens")
        return normalized

    @field_validator("language")
    @classmethod
    def validate_language(cls, value: str) -> str:
        normalized = value.strip().lower()
        if normalized not in _SUPPORTED_LANGUAGES:
            raise ValueError("language must be 'vi' or 'en'")
        return normalized

    @field_validator("place_name", "title", "body", "cultural_hook")
    @classmethod
    def strip_text(cls, value: str) -> str:
        return value.strip()

    @field_validator("weather_tags")
    @classmethod
    def validate_weather(cls, values: list[str]) -> list[str]:
        return _normalize_against(values, WEATHER_TAGS, "weather_tags")

    @field_validator("time_tags")
    @classmethod
    def validate_time(cls, values: list[str]) -> list[str]:
        return _normalize_against(values, TIME_TAGS, "time_tags")

    @field_validator("season_tags")
    @classmethod
    def validate_season(cls, values: list[str]) -> list[str]:
        return _normalize_against(values, SEASON_TAGS, "season_tags")

    @field_validator("categories", "tags")
    @classmethod
    def normalize_slug_list(cls, values: list[str]) -> list[str]:
        out: list[str] = []
        for raw in values:
            value = str(raw).strip().lower()
            if value and value not in out:
                out.append(value)
        return out


class SpotlightPostCollection(BaseModel):
    """Versioned collection wrapper for curated spotlight JSON files."""

    version: int = Field(default=1, ge=1)
    dataset_id: str = Field(min_length=3, max_length=120)
    dataset_name: str = Field(min_length=1, max_length=160)
    description: str = Field(default="", max_length=500)
    posts: list[SpotlightPost] = Field(min_length=1)

    @field_validator("dataset_id")
    @classmethod
    def validate_dataset_id(cls, value: str) -> str:
        normalized = value.strip().lower()
        if not _SLUG_PATTERN.fullmatch(normalized):
            raise ValueError("dataset_id must be a lowercase slug")
        return normalized

    @model_validator(mode="after")
    def validate_unique_post_ids(self) -> "SpotlightPostCollection":
        ids = [p.post_id for p in self.posts]
        dups = sorted(i for i in set(ids) if ids.count(i) > 1)
        if dups:
            raise ValueError(f"duplicate post_id values: {dups}")
        return self


def _normalize_against(values: list[str], allowed: set[str], field: str) -> list[str]:
    """Lowercase, dedupe, and restrict a tag list to its controlled vocab."""
    out: list[str] = []
    for raw in values:
        value = str(raw).strip().lower()
        if not value:
            continue
        if value not in allowed:
            raise ValueError(f"{field}: '{value}' not in {sorted(allowed)}")
        if value not in out:
            out.append(value)
    return out
