from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.services.capture_service import CaptureService
from app.schemas.schemas import CaptureCreateDBRequest, CaptureDBResponse
from app.api.deps import get_current_user
from app.models.user import User

router = APIRouter()

@router.post("/captures", response_model=CaptureDBResponse, status_code=201, tags=["Captures"])
def create_capture(
    body: CaptureCreateDBRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    return CaptureService(db).create_capture(
        user_id=current_user.id,
        place_id=body.place_id,
        task_id=body.task_id,
        timestamp=body.timestamp,
        latitude=body.latitude,
        longitude=body.longitude,
        media_url=body.media_url,
        note=body.note,
    )