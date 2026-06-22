"""Preference schemas — API contract for per-user personalization settings."""
from typing import List, Optional

from pydantic import BaseModel, Field


class PreferenceSchema(BaseModel):
    """User-editable personalization signals."""
    interests: List[str] = Field(
        default_factory=list,
        description="Category tags the user cares about, e.g. history, architecture, cuisine.",
    )
    home_latitude: Optional[float] = None
    home_longitude: Optional[float] = None
