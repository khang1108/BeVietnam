"""Capture media upload to MinIO (JWT-protected)."""
from fastapi import APIRouter, Depends, File, HTTPException, UploadFile
from fastapi.concurrency import run_in_threadpool

from ...core import minio_client
from ...models.models import UserModel
from ..dependencies import get_current_user

router = APIRouter(prefix="/uploads", tags=["Uploads"])

_ALLOWED = {"image/jpeg": "jpg", "image/png": "png", "image/webp": "webp"}


@router.post("/capture")
async def upload_capture(
    file: UploadFile = File(...),
    current_user: UserModel = Depends(get_current_user),
):
    """Upload a capture image to MinIO; returns the media_url to save on a capture."""
    ext = _ALLOWED.get(file.content_type or "")
    if not ext:
        raise HTTPException(400, "Only JPEG, PNG, or WebP images are allowed")

    data = await file.read()
    await run_in_threadpool(minio_client.ensure_bucket)
    media_url = await run_in_threadpool(
        minio_client.upload_file, data, file.content_type, ext
    )
    return {"media_url": media_url}
