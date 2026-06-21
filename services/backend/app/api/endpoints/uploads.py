"""Upload endpoints — capture media to MinIO (JWT-protected)."""

import uuid

from fastapi import APIRouter, Depends, File, HTTPException, UploadFile, status
from fastapi.concurrency import run_in_threadpool

from services.backend.app.api.dependencies import get_current_user
from services.backend.app.core.minio_client import minio_client
from services.backend.app.models.models import UserModel

router = APIRouter(prefix="/uploads", tags=["Uploads"])

_ALLOWED = {
    "image/jpeg": "jpg",
    "image/png": "png",
    "image/webp": "webp",
}
_MAX_BYTES = 10 * 1024 * 1024  # 10 MB


@router.post("/capture", status_code=status.HTTP_201_CREATED)
async def upload_capture(
    file: UploadFile = File(...),
    current_user: UserModel = Depends(get_current_user),
):
    """Store a capture image in MinIO and return its URL.

    Requires a bearer JWT. Accepts jpeg/png/webp up to 10 MB.
    """
    ext = _ALLOWED.get(file.content_type or "")
    if ext is None:
        raise HTTPException(
            status_code=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
            detail=f"Unsupported content type: {file.content_type}. Allowed: {', '.join(_ALLOWED)}",
        )

    data = await file.read()
    if len(data) > _MAX_BYTES:
        raise HTTPException(
            status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
            detail="File exceeds the 10 MB limit.",
        )

    key = f"{current_user.id}/{uuid.uuid4().hex}.{ext}"
    media_url = await run_in_threadpool(
        minio_client.upload_bytes, key, data, file.content_type
    )
    return {"media_url": media_url, "key": key, "content_type": file.content_type}
