import uuid
from functools import lru_cache

import boto3
from botocore.client import Config

from app.core.config import settings

BUCKET = "bevietnam-captures"


@lru_cache(maxsize=1)
def _client():
    return boto3.client(
        "s3",
        endpoint_url=settings.MINIO_ENDPOINT,        # vi du: http://localhost:9000
        aws_access_key_id=settings.MINIO_ACCESS_KEY,
        aws_secret_access_key=settings.MINIO_SECRET_KEY,
        config=Config(signature_version="s3v4"),
        region_name="us-east-1",
    )


def ensure_bucket() -> None:
    """Tao bucket neu chua co. Goi 1 lan luc app startup."""
    client = _client()
    existing = [b["Name"] for b in client.list_buckets().get("Buckets", [])]
    if BUCKET not in existing:
        client.create_bucket(Bucket=BUCKET)


def upload_file(file_bytes: bytes, content_type: str, ext: str) -> str:
    """Upload bytes len MinIO, tra ve URL cong khai luu vao cot media_url."""
    object_name = f"{uuid.uuid4()}.{ext}"
    _client().put_object(
        Bucket=BUCKET,
        Key=object_name,
        Body=file_bytes,
        ContentType=content_type,
    )
    return f"{settings.MINIO_ENDPOINT}/{BUCKET}/{object_name}"
