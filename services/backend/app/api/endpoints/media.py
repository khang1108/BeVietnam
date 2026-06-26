"""Public media proxy for objects stored in MinIO."""

from fastapi import APIRouter, HTTPException, Response, status
from fastapi.concurrency import run_in_threadpool
from botocore.exceptions import ClientError

from services.backend.app.core.minio_client import minio_client

router = APIRouter(prefix="/media", tags=["Media"])


@router.get("/{key:path}")
async def get_media(key: str):
    """Serve a MinIO object through the backend public API."""
    try:
        data, content_type = await run_in_threadpool(minio_client.get_bytes, key)
    except ClientError as exc:
        code = exc.response.get("Error", {}).get("Code")
        if code in {"NoSuchKey", "404"}:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Media not found") from exc
        raise HTTPException(status_code=status.HTTP_502_BAD_GATEWAY, detail="Media storage error") from exc

    return Response(content=data, media_type=content_type)
