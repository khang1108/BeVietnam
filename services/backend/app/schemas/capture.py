"""Capture schemas — API contract for photo capture endpoints."""
from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime


class CaptureCreateRequest(BaseModel):
    task_id: Optional[str] = None
    place_id: str
    timestamp: Optional[datetime] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    media_url: Optional[str] = Field(None, description="URL hoặc key ảnh/video")
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
