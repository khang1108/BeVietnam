from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime


# ── Health ────────────────────────────────────────────────────────────────────
class HealthResponse(BaseModel):
    status: str = "ok"
    version: str
    timestamp: datetime


# ── Place ─────────────────────────────────────────────────────────────────────
class PlaceSchema(BaseModel):
    id: str
    name: str
    category: str                        # e.g. "temple", "museum", "park"
    description: str
    latitude: float
    longitude: float
    image_url: Optional[str] = None
    reference_url: Optional[str] = None

class PlacesResponse(BaseModel):
    total: int
    items: List[PlaceSchema]


# ── Feed ──────────────────────────────────────────────────────────────────────
class FeedItem(BaseModel):
    id: str
    place_id: str
    name: str
    category: str
    thumbnail_url: Optional[str] = None
    score: float = Field(ge=0.0, le=1.0, description="Điểm gợi ý (0.0 - 1.0)")
    explanation: str = Field(description="Giải thích tại sao gợi ý địa điểm này")
    created_at: datetime

class FeedResponse(BaseModel):
    items: List[FeedItem]


# ── Storyline ─────────────────────────────────────────────────────────────────
class StorylineTask(BaseModel):
    task_id: str
    title: str
    description: str
    cultural_explanation: str = Field(description="Giải thích văn hoá/lịch sử")
    difficulty: str = Field(description="easy | medium | hard")
    completion_requirement: str = Field(description="Điều kiện hoàn thành task")
    place_id: Optional[str] = None
    score: float = Field(ge=0.0, le=1.0)

class StorylineNextTaskResponse(BaseModel):
    task: StorylineTask
    ai_generated: bool
    fallback: bool = False


# ── Captures ──────────────────────────────────────────────────────────────────
class CaptureCreateRequest(BaseModel):
    user_id: str
    task_id: Optional[str] = None
    place_id: str
    timestamp: Optional[datetime] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    media_url: Optional[str] = Field(None, description="URL hoặc key ảnh/video placeholder")
    note: Optional[str] = None

class CaptureResponse(BaseModel):
    id: str
    user_id: str
    task_id: Optional[str]
    place_id: str
    timestamp: datetime
    latitude: Optional[float]
    longitude: Optional[float]
    media_url: Optional[str]
    note: Optional[str]
    created_at: datetime


# ── Logs ─────────────────────────────────────────────────────────────────────
class LogEntry(BaseModel):
    level: str
    message: str
    timestamp: datetime

class LogsResponse(BaseModel):
    logs: List[LogEntry]
