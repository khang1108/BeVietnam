from fastapi import APIRouter, Depends
from services.backend.app.api.dependencies import get_current_user
from services.backend.app.models.models import UserModel
from services.backend.app.schemas.capture import CaptureCreateRequest, CaptureResponse
from services.backend.app.services.services import capture_service

router = APIRouter()


@router.post("/captures", response_model=CaptureResponse, status_code=201, tags=["Captures"])
async def create_capture(
    body: CaptureCreateRequest,
    current_user: UserModel = Depends(get_current_user),
):
    """
    POST /captures — Lưu metadata ảnh chụp tại địa điểm cho user đang đăng nhập.

    user_id lấy từ JWT (không nhận từ client).
    Nhận: task_id, place_id, timestamp, latitude, longitude, media_url, note.
    Sprint 1: lưu in-memory. Sprint 2: kết nối DB.
    """
    return await capture_service.create_capture(current_user.id, body)
