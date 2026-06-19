from datetime import datetime, timezone
from fastapi import APIRouter, Depends, File, Form, HTTPException, UploadFile, status
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.minio_client import upload_file
from app.api.dependencies import get_current_user
from app.services.capture_service import CaptureService
from app.repositories.capture_repository import CaptureRepository
from app.schemas.schemas import CaptureDBResponse
from app.models.user import User

router = APIRouter()
_ALLOWED_CONTENT_TYPES = {"image/jpeg", "image/png", "image/webp"}

def _ext_from(filename, content_type):
    ext_map = {"image/jpeg": "jpg", "image/png": "png", "image/webp": "webp"}
    if filename and "." in filename:
        return filename.rsplit(".", 1)[-1].lower()
    return ext_map.get(content_type, "jpg")

@router.post("/captures", response_model=CaptureDBResponse, status_code=201, tags=["Captures"])
def create_capture(
    file: UploadFile = File(...),
    place_id: int = Form(...),
    task_id: str = Form(None),
    latitude: float = Form(None),
    longitude: float = Form(None),
    note: str = Form(None),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    if file.content_type not in _ALLOWED_CONTENT_TYPES:
        raise HTTPException(status_code=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE, detail=f"Unsupported file type: {file.content_type}")
    file_bytes = file.file.read()
    ext = _ext_from(file.filename, file.content_type)
    media_url = upload_file(file_bytes=file_bytes, content_type=file.content_type, ext=ext)
    return CaptureService(db).create_capture(
        user_id=current_user.id, place_id=place_id, task_id=task_id,
        timestamp=datetime.now(timezone.utc), latitude=latitude,
        longitude=longitude, media_url=media_url, note=note,
    )

@router.get("/captures", response_model=list[CaptureDBResponse], tags=["Captures"])
def list_my_captures(db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    return CaptureRepository(db).get_by_user(current_user.id)

@router.get("/captures/{capture_id}", response_model=CaptureDBResponse, tags=["Captures"])
def get_capture(capture_id: int, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    capture = CaptureRepository(db).get_by_id(capture_id)
    if not capture or capture.user_id != current_user.id:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Capture not found")
    return capture