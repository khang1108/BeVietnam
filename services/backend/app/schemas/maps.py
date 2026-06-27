"""Map config schemas for backend-hosted MapLibre styles."""

from pydantic import BaseModel


class MapConfigResponse(BaseModel):
    enabled: bool
    style_url: str | None = None
    initial_latitude: float = 16.047079
    initial_longitude: float = 108.206230
    initial_zoom: float = 5.5
