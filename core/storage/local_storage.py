import os
import shutil
from pathlib import Path
from typing import BinaryIO, Optional


class LocalStorage:
    """Local filesystem storage."""

    def __init__(self, upload_folder: str):
        self.upload_folder = Path(upload_folder)

    def get_default_bucket(self) -> str:
        return "local"

    def _ensure_path(self, bucket: str, file_key: str) -> Path:
        full_path = self.upload_folder / bucket / file_key
        full_path.parent.mkdir(parents=True, exist_ok=True)
        return full_path

    def store(self, bucket: str, file_key: str, data: bytes) -> str:
        path = self._ensure_path(bucket, file_key)
        path.write_bytes(data)
        return str(path)

    def store_stream(self, bucket: str, file_key: str, stream: BinaryIO) -> str:
        path = self._ensure_path(bucket, file_key)
        with open(path, "wb") as f:
            shutil.copyfileobj(stream, f)
        return str(path)

    def get_bytes(self, bucket: str, file_key: str) -> bytes:
        path = self.upload_folder / bucket / file_key
        return path.read_bytes()

    def get_url(self, bucket: str, file_key: str) -> str:
        return str(self.upload_folder / bucket / file_key)

    def get_auth_url(self, bucket: str, file_key: str, timeout_ms: int = 60000) -> str:
        return self.get_url(bucket, file_key)

    def delete(self, bucket: str, file_key: str) -> None:
        path = self.upload_folder / bucket / file_key
        if path.exists():
            path.unlink()

    def exists(self, bucket: str, file_key: str) -> bool:
        return (self.upload_folder / bucket / file_key).exists()

    def copy(self, src_bucket: str, src_key: str, dst_bucket: str, dst_key: str) -> None:
        src = self.upload_folder / src_bucket / src_key
        dst = self.upload_folder / dst_bucket / dst_key
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
