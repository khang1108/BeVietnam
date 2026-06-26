"""MinIO / S3-compatible storage client for capture media.

Thin boto3 wrapper. All calls are blocking (boto3 is sync) — endpoints must run
them in a threadpool so they don't block the async event loop.
"""

import logging

import boto3
from botocore.client import Config as BotoConfig
from botocore.exceptions import ClientError

from services.backend.app.core.config import settings

logger = logging.getLogger(__name__)


class MinioClient:
    """Stores capture images in an S3-compatible bucket and returns their URL."""

    def __init__(self) -> None:
        self._client = boto3.client(
            "s3",
            endpoint_url=settings.MINIO_ENDPOINT,
            aws_access_key_id=settings.MINIO_ACCESS_KEY,
            aws_secret_access_key=settings.MINIO_SECRET_KEY,
            config=BotoConfig(signature_version="s3v4"),
        )
        self._bucket = settings.MINIO_BUCKET

    def ensure_bucket(self) -> None:
        """Create the bucket if it does not already exist (idempotent)."""
        try:
            self._client.head_bucket(Bucket=self._bucket)
        except ClientError:
            self._client.create_bucket(Bucket=self._bucket)
            logger.info("Created MinIO bucket: %s", self._bucket)

    def upload_bytes(self, key: str, data: bytes, content_type: str) -> str:
        """Upload raw bytes under `key`; return the object URL."""
        self.ensure_bucket()
        self._client.put_object(
            Bucket=self._bucket,
            Key=key,
            Body=data,
            ContentType=content_type,
        )
        return self.object_url(key)

    def get_bytes(self, key: str) -> tuple[bytes, str]:
        """Read an object from storage and return (bytes, content_type)."""
        obj = self._client.get_object(Bucket=self._bucket, Key=key)
        data = obj["Body"].read()
        content_type = obj.get("ContentType") or "application/octet-stream"
        return data, content_type

    def object_url(self, key: str) -> str:
        """Return the public API URL for a stored object key."""
        base = settings.PUBLIC_API_BASE_URL.rstrip("/")
        return f"{base}/media/{key}"


minio_client = MinioClient()
