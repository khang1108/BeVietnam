from fastapi import APIRouter
from src.bevietnam.backend.app.schemas.capture import CaptureCreateRequest, CaptureResponse
from src.bevietnam.backend.app.services.services import capture_service

router = APIRouter()


@router.post("/captures", response_model=CaptureResponse, status_code=201, tags=["Captures"])
async def create_capture(body: CaptureCreateRequest):
    """
    POST /captures — Lưu metadata ảnh chụp tại địa điểm.

    Nhận: user_id, task_id, place_id, timestamp, latitude, longitude, media_url, note.
    Sprint 1: lưu in-memory. Sprint 2: kết nối DB.
    """
    return await capture_service.create_capture(body)
