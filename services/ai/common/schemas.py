"""
AI Core Pydantic Schemas — Request/Response contracts.

These schemas define the API contract between the Backend and AI Core.
Both sides must agree on these shapes for reliable communication.

All schemas use Pydantic for automatic validation and serialization.
"""

from pydantic import BaseModel, Field


# ── Task Generation ──────────────────────────────────────────────────────────

class GenerateTaskRequest(BaseModel):
    """Request body for POST /generate-task."""

    user_id: str
    latitude: float | None = None
    longitude: float | None = None
    interests: list[str] = []
    quest_state: dict = Field(
        default_factory=dict,
        description="Current quest chain state (completed tasks, current task, step index)",
    )
    nearby_places: list[dict] = Field(
        default_factory=list,
        description="Places near the user, provided by backend",
    )
    language: str = Field(
        default="vi",
        description="Preferred output language: 'vi' or 'en'",
    )


class GeneratedTask(BaseModel):
    """Schema for a generated cultural exploration task."""

    quest_id: str = ""
    task_id: str = ""
    step_index: int = 1
    title: str
    description: str
    cultural_explanation: str
    completion_requirement: str
    unlock_condition: dict = Field(default_factory=dict)
    next_task_hint: str = ""
    difficulty: str = "easy"
    reason_codes: list[str] = []


# ── Question Pool Generation ─────────────────────────────────────────────────

class GenerateQuestionPoolRequest(BaseModel):
    """Request body for POST /generate-question-pool."""

    facts: list[dict] = Field(
        default_factory=list,
        description="Grounded cultural facts extracted from books.",
    )
    place_name: str = ""
    language: str = "vi"
    max_questions: int = Field(default=20, ge=1, le=100)


# ── Recommendation Explanation ────────────────────────────────────────────────

class ExplainRecommendationRequest(BaseModel):
    """Request body for POST /explain-recommendation."""

    user_id: str
    place: dict = Field(
        default_factory=dict,
        description="Place context: place_id, name, category, lat, lng, optional priority (0–100).",
    )
    interests: list[str] = []
    context: dict = Field(
        default_factory=dict,
        description="Normalized backend scores: weather_score, traffic_score, "
        "distance_score, crowd_score (0–100), and optional missing_factors.",
    )
    language: str = Field(default="vi", description="Preferred language: 'vi' or 'en'.")


class RecommendationResult(BaseModel):
    """Response payload for recommendation scoring + grounded explanation."""

    place_id: str = ""
    suitability_score: int = 0
    culture_score: int = 0
    culture_components: dict = Field(default_factory=dict)
    bubble_size: str = "small"  # "small" | "medium" | "large"
    explanation: str = ""
    cultural_highlight: str = ""
    reason_codes: list[str] = []
    source_refs: list[dict] = []
    missing_factors: list[str] = []
    fallback: bool = False
    ai_generated: bool = False
    confidence: float = 0.0


# ── Capture Verification ─────────────────────────────────────────────────────

class VerifyCaptureRequest(BaseModel):
    """Request body for POST /verify-capture."""

    user_id: str
    task: dict
    capture: dict


class CaptureVerification(BaseModel):
    """Response for capture verification."""

    status: str  # "approved", "rejected", "needs_review"
    reason: str
    confidence: float


# ── Vlog Generation ──────────────────────────────────────────────────────────

class GenerateVlogRequest(BaseModel):
    """Request body for POST /generate-vlog."""

    user_id: str
    local_date: str
    captures: list[dict] = []


class GeneratedVlog(BaseModel):
    """Response for generated travel memory."""

    title: str
    summary: str
    body: str


# ── Quest Chain (for full chain retrieval) ────────────────────────────────────

class QuestChainRequest(BaseModel):
    """Request body for POST /quest-chain."""

    user_id: str
    quest_id: str = ""
    place_name: str = ""
