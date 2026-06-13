import io
from typing import BinaryIO, Optional

import boto3
from botocore.exceptions import ClientError
from .interface import ChunkInfo, ChunkedUploader, FileStorageInterface


class S3Storage(FileStorageInterface, ChunkedUploader):
    """Generic S3-compatible object storage (AWS S3, RustFS, etc.)."""

    def __init__(self, endpoint: str, access_key: str, secret_key: str,
                 default_bucket: str, region: str = "us-east-1",
                 path_style: bool = True, base_url: str = ""):
        self._default_bucket = default_bucket
        self._base_url = base_url
        self._endpoint = endpoint
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
        if self._base_url:
            return f"{self._base_url.rstrip('/')}/{bucket}/{file_key}"
        return f"{self._endpoint.rstrip('/')}/{bucket}/{file_key}"

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

    def init_chunk_upload(self, bucket: str, file_key: str, total_chunks: int) -> str:
        self._ensure_bucket(bucket)
        response = self._client.create_multipart_upload(Bucket=bucket, Key=file_key)
        return str(response["UploadId"])

    def upload_chunk(self, bucket: str, file_key: str, upload_id: str, chunk: ChunkInfo) -> None:
        if chunk.data is None:
            raise ValueError("chunk data is required")
        kwargs = {
            "Bucket": bucket,
            "Key": file_key,
            "UploadId": upload_id,
            "PartNumber": chunk.chunk_index + 1,
            "Body": chunk.data,
        }
        if chunk.size > 0:
            kwargs["ContentLength"] = chunk.size
        self._client.upload_part(**kwargs)

    def complete_chunk_upload(self, bucket: str, file_key: str, upload_id: str) -> str:
        parts_response = self._client.list_parts(Bucket=bucket, Key=file_key, UploadId=upload_id)
        parts = [
            {"PartNumber": item["PartNumber"], "ETag": item["ETag"]}
            for item in parts_response.get("Parts", [])
        ]
        if not parts:
            raise ValueError("no parts to complete")
        self._client.complete_multipart_upload(
            Bucket=bucket,
            Key=file_key,
            UploadId=upload_id,
            MultipartUpload={"Parts": parts},
        )
        return f"{bucket}/{file_key}"

    def abort_chunk_upload(self, bucket: str, file_key: str, upload_id: str) -> None:
        self._client.abort_multipart_upload(Bucket=bucket, Key=file_key, UploadId=upload_id)
