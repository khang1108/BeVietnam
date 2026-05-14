from fastapi import APIRouter
from datetime import datetime, timezone
import uuid
from app.schemas.schemas import CaptureCreateRequest, CaptureResponse

router = APIRouter()

# In-memory store — thay bằng DB (Backend Engineer 1) sau Sprint 1
_captures: list[CaptureResponse] = []


@router.post("/captures", response_model=CaptureResponse, status_code=201, tags=["Captures"])
async def create_capture(body: CaptureCreateRequest):
    """
    POST /captures — Lưu metadata ảnh chụp tại địa điểm.

    Nhận: user_id, task_id, place_id, timestamp, latitude, longitude, media_url, note.
    Android submit capture metadata cho Sprint 1/2 demo dù chưa có storage thật.
    """
    now = datetime.now(timezone.utc)
    capture = CaptureResponse(
        id=str(uuid.uuid4()),
        user_id=body.user_id,
        task_id=body.task_id,
        place_id=body.place_id,
        timestamp=body.timestamp or now,
        latitude=body.latitude,
        longitude=body.longitude,
        media_url=body.media_url,
        note=body.note,
        created_at=now,
    )
    _captures.append(capture)
    return capture
