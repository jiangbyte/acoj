import io
from typing import BinaryIO, Optional

import boto3
from botocore.exceptions import ClientError


class S3Storage:
    """Generic S3-compatible object storage (AWS S3, RustFS, etc.)."""

    def __init__(self, endpoint: str, access_key: str, secret_key: str,
                 default_bucket: str, region: str = "us-east-1",
                 path_style: bool = True):
        self._default_bucket = default_bucket
        self._client = boto3.client(
            "s3",
            endpoint_url=endpoint,
            aws_access_key_id=access_key,
            aws_secret_access_key=secret_key,
            region_name=region,
            config=boto3.session.Config(
                s3={"addressing_style": "path" if path_style else "virtual"},
                signature_version="s3v4",
            ),
        )

    def get_default_bucket(self) -> str:
        return self._default_bucket

    def _ensure_bucket(self, bucket: str):
        try:
            self._client.head_bucket(Bucket=bucket)
        except ClientError:
            self._client.create_bucket(Bucket=bucket)

    def store(self, bucket: str, file_key: str, data: bytes) -> str:
        self._ensure_bucket(bucket)
        self._client.put_object(Bucket=bucket, Key=file_key, Body=data)
        return f"{bucket}/{file_key}"

    def store_stream(self, bucket: str, file_key: str, stream: BinaryIO) -> str:
        self._ensure_bucket(bucket)
        self._client.upload_fileobj(stream, bucket, file_key)
        return f"{bucket}/{file_key}"

    def get_bytes(self, bucket: str, file_key: str) -> bytes:
        response = self._client.get_object(Bucket=bucket, Key=file_key)
        return response["Body"].read()

    def get_url(self, bucket: str, file_key: str) -> str:
        return f"{self._client.meta.endpoint_url}/{bucket}/{file_key}"

    def get_auth_url(self, bucket: str, file_key: str, timeout_ms: int = 60000) -> str:
        return self._client.generate_presigned_url(
            "get_object",
            Params={"Bucket": bucket, "Key": file_key},
            ExpiresIn=timeout_ms // 1000,
        )

    def delete(self, bucket: str, file_key: str) -> None:
        try:
            self._client.delete_object(Bucket=bucket, Key=file_key)
        except ClientError:
            pass

    def exists(self, bucket: str, file_key: str) -> bool:
        try:
            self._client.head_object(Bucket=bucket, Key=file_key)
            return True
        except ClientError:
            return False

    def copy(self, src_bucket: str, src_key: str, dst_bucket: str, dst_key: str) -> None:
        self._client.copy_object(
            Bucket=dst_bucket, Key=dst_key,
            CopySource={"Bucket": src_bucket, "Key": src_key},
        )
