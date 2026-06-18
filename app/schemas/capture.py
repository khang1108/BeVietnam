from datetime import datetime
from pydantic import BaseModel, ConfigDict


class CaptureResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    user_id: int
    place_id: int
    task_id: str | None = None
    timestamp: datetime | None = None
    latitude: float | None = None
    longitude: float | None = None
    media_url: str | None = None
    note: str | None = None
    created_at: datetime