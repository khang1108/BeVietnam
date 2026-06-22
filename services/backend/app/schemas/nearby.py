"""Nearby POI schemas — live Foursquare places shown on the explore map."""
from typing import List, Optional

from pydantic import BaseModel


class NearbyPlace(BaseModel):
    id: str
    name: str
    latitude: float
    longitude: float
    category: str  # our coarse bucket: food | lodging | culture | history | nature | place
    category_label: str  # Foursquare's own category name, for display
    address: Optional[str] = None
    distance_meters: Optional[int] = None


class NearbyResponse(BaseModel):
    total: int
    items: List[NearbyPlace]
