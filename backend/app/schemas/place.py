"""Place schemas — API contract for place-related endpoints."""
from pydantic import BaseModel, Field
from typing import Optional, List


class PlaceCreateRequest(BaseModel):
    place_id: Optional[str] = Field(None, alias="place_id")
    name: str
    category: str
    description: str
    latitude: float
    longitude: float
    image_url: Optional[str] = None
    reference_url: Optional[str] = None

    model_config = {
        "populate_by_name": True,
    }


class PlaceSchema(BaseModel):
    id: str
    name: str
    category: str  # "temple" | "museum" | "park" | "district" | ...
    description: str
    latitude: float
    longitude: float
    image_url: Optional[str] = None
    reference_url: Optional[str] = None

    model_config = {
        "from_attributes": True,
    }


class PlaceImportRequest(BaseModel):
    file_path: Optional[str] = None
    items: List[PlaceCreateRequest] = Field(default_factory=list)

    model_config = {
        "populate_by_name": True,
    }


class PlacesResponse(BaseModel):
    total: int
    items: List[PlaceSchema]
