import io
from typing import BinaryIO, Optional

from minio import Minio
from minio.error import S3Error


class MinioStorage:
    """MinIO S3-compatible storage."""

    def __init__(self, endpoint: str, access_key: str, secret_key: str,
                 default_bucket: str, secure: bool = False, region: Optional[str] = None):
        self._default_bucket = default_bucket
        self._client = Minio(
            endpoint=endpoint,
            access_key=access_key,
            secret_key=secret_key,
            secure=secure,
            region=region,
        )

    def get_default_bucket(self) -> str:
        return self._default_bucket

    def _ensure_bucket(self, bucket: str):
        if not self._client.bucket_exists(bucket):
            self._client.make_bucket(bucket)

    def store(self, bucket: str, file_key: str, data: bytes) -> str:
        self._ensure_bucket(bucket)
        self._client.put_object(
            bucket, file_key, io.BytesIO(data), length=len(data),
        )
        return f"{bucket}/{file_key}"

    def store_stream(self, bucket: str, file_key: str, stream: BinaryIO) -> str:
        self._ensure_bucket(bucket)
        data = stream.read()
        self._client.put_object(
            bucket, file_key, io.BytesIO(data), length=len(data),
        )
        return f"{bucket}/{file_key}"

    def get_bytes(self, bucket: str, file_key: str) -> bytes:
        response = self._client.get_object(bucket, file_key)
        try:
            return response.read()
        finally:
            response.close()

    def get_url(self, bucket: str, file_key: str) -> str:
        return f"{self._client._endpoint_url}/{bucket}/{file_key}"

    def get_auth_url(self, bucket: str, file_key: str, timeout_ms: int = 60000) -> str:
        return self._client.presigned_get_object(bucket, file_key, expires=timeout_ms // 1000)

    def delete(self, bucket: str, file_key: str) -> None:
        try:
            self._client.remove_object(bucket, file_key)
        except S3Error:
            pass

    def exists(self, bucket: str, file_key: str) -> bool:
        try:
            self._client.stat_object(bucket, file_key)
            return True
        except S3Error:
            return False

    def copy(self, src_bucket: str, src_key: str, dst_bucket: str, dst_key: str) -> None:
        self._client.copy_object(
            dst_bucket, dst_key,
            f"{src_bucket}/{src_key}",
        )
