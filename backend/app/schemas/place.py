"""Place schemas — API contract for place-related endpoints."""
from pydantic import BaseModel
from typing import Optional, List


class PlaceSchema(BaseModel):
    id: str
    name: str
    category: str  # "temple" | "museum" | "park" | "district" | ...
    description: str
    latitude: float
    longitude: float
    image_url: Optional[str] = None
    reference_url: Optional[str] = None


class PlacesResponse(BaseModel):
    total: int
    items: List[PlaceSchema]
