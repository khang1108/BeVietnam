"""MinIO (S3-compatible) object storage for capture media.

boto3 S3 client pointed at a MinIO endpoint. Calls are blocking; invoke from
async code via fastapi.concurrency.run_in_threadpool.
"""
import uuid
from functools import lru_cache

import boto3
from botocore.client import Config

from .config import settings

BUCKET = "bevietnam-captures"


@lru_cache(maxsize=1)
def _client():
    return boto3.client(
        "s3",
        endpoint_url=settings.MINIO_ENDPOINT,
        aws_access_key_id=settings.MINIO_ACCESS_KEY,
        aws_secret_access_key=settings.MINIO_SECRET_KEY,
        config=Config(signature_version="s3v4"),
        region_name="us-east-1",
    )


def ensure_bucket() -> None:
    """Create the bucket if it does not exist. Call once at app startup."""
    client = _client()
    existing = [b["Name"] for b in client.list_buckets().get("Buckets", [])]
    if BUCKET not in existing:
        client.create_bucket(Bucket=BUCKET)


def upload_file(file_bytes: bytes, content_type: str, ext: str) -> str:
    """Upload bytes to MinIO; return the public URL to store in media_url."""
    object_name = f"{uuid.uuid4()}.{ext}"
    _client().put_object(
        Bucket=BUCKET,
        Key=object_name,
        Body=file_bytes,
        ContentType=content_type,
    )
    return f"{settings.MINIO_ENDPOINT}/{BUCKET}/{object_name}"
