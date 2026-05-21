"""Feed schemas — API contract for feed/recommendation endpoints."""
from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime


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
