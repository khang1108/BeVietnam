"""Question pool schemas for context-aware cultural task selection."""

from datetime import datetime
from typing import Any

from pydantic import BaseModel, Field


class QuestionPoolItem(BaseModel):
    """Reusable cultural question/task generated from curated book facts."""

    question_id: str
    title: str
    question_text: str
    cultural_explanation: str
    source_text: str = ""
    source: str = ""
    place_name: str = ""
    latitude: float | None = None
    longitude: float | None = None
    radius_meters: int = 1200
    categories: list[str] = Field(default_factory=list)
    difficulty: str = "easy"
    required_media: str = "photo"
    indoor_outdoor: str = "any"
    weather_tags: list[str] = Field(default_factory=lambda: ["any"])
    time_tags: list[str] = Field(default_factory=lambda: ["any"])
    estimated_duration_minutes: int = 15
    language: str = "vi"
    metadata: dict[str, Any] = Field(default_factory=dict)


class RuntimeContextRequest(BaseModel):
    """Current user context used to select a suitable question."""

    user_id: str = "demo-user"
    latitude: float
    longitude: float
    weather: str | None = Field(
        default=None,
        description="Optional client/weather-provider condition: sunny | rainy | hot | cloudy | any",
    )
    time_of_day: str | None = Field(
        default=None,
        description="Optional current period: morning | afternoon | evening | night",
    )
    interests: list[str] = Field(default_factory=list)
    completed_question_ids: list[str] = Field(default_factory=list)
    limit: int = Field(default=1, ge=1, le=10)


class RuntimeContext(BaseModel):
    """Resolved context after applying Goong and local fallbacks."""

    latitude: float
    longitude: float
    weather: str
    time_of_day: str
    formatted_address: str = ""
    place_name: str = ""
    source: str = "local"
    resolved_at: datetime


class SelectedQuestion(BaseModel):
    """Question plus ranking diagnostics."""

    question: QuestionPoolItem
    score: float
    reasons: list[str] = Field(default_factory=list)
    distance_meters: float | None = None


class SelectQuestionResponse(BaseModel):
    """Response returned to web/mobile for the next context-aware task."""

    context: RuntimeContext
    selected: list[SelectedQuestion]
    total_pool_size: int
    fallback: bool = False


class QuestionPoolResponse(BaseModel):
    """List the currently loaded question pool."""

    total: int
    items: list[QuestionPoolItem]
    source_path: str
    fallback: bool = False
