"""Storage abstraction aligned to hei-gin's factory semantics."""

from __future__ import annotations

from dataclasses import dataclass
from typing import Optional
from sdk.config.settings import settings

from .interface import ChunkInfo, ChunkedUploader, FileStorageInterface

__all__ = [
    "FileStorageInterface", "ChunkInfo", "ChunkedUploader",
    "StorageConfig", "get_config", "get_storage", "get_default_storage",
    "LocalStorage", "MinioStorage", "S3Storage",
    "get_url",
]


@dataclass(slots=True)
class StorageConfig:
    default: str
    default_base_url: str
    local: object
    minio: object
    s3: object


def get_config() -> StorageConfig:
    return StorageConfig(
        default=settings.storage.default,
        default_base_url=settings.storage.default_base_url,
        local=settings.storage.local,
        minio=settings.storage.minio,
        s3=settings.storage.s3,
    )


def get_storage(storage_type: str) -> Optional[FileStorageInterface]:
    cfg = get_config()
    if storage_type == "LOCAL":
        return LocalStorage(cfg.local.upload_folder, cfg.local.base_url)
    if storage_type == "MINIO":
        m = cfg.minio
        if not m.endpoint:
            return None
        endpoint = m.endpoint
        if not endpoint.startswith(("http://", "https://")):
            endpoint = ("https://" if m.secure else "http://") + endpoint
        return S3Storage(endpoint, m.access_key, m.secret_key,
                         m.bucket, m.region, True, m.base_url)
    if storage_type == "S3":
        s = cfg.s3
        if not s.endpoint:
            return None
        return S3Storage(s.endpoint, s.access_key, s.secret_key,
                         s.bucket, s.region, s.path_style, s.base_url)
    return LocalStorage(cfg.local.upload_folder, cfg.local.base_url)


def get_default_storage() -> FileStorageInterface:
    cfg = get_config()
    result = get_storage(cfg.default)
    if result is None:
        return LocalStorage(cfg.local.upload_folder, cfg.local.base_url)
    return result


def LocalStorage(upload_folder: str = "./uploads", base_url: str = ""):
    from .local_storage import LocalStorage as _cls
    return _cls(upload_folder, base_url)


def MinioStorage(endpoint: str, access_key: str, secret_key: str,
                 bucket: str = "hei", secure: bool = False,
                 region: str = "us-east-1", base_url: str = ""):
    if not endpoint.startswith(("http://", "https://")):
        endpoint = ("https://" if secure else "http://") + endpoint
    return S3Storage(endpoint, access_key, secret_key, bucket, region, True, base_url)


def S3Storage(endpoint: str, access_key: str, secret_key: str,
              bucket: str = "hei", region: str = "us-east-1",
              path_style: bool = True, base_url: str = ""):
    from .s3_storage import S3Storage as _cls
    return _cls(endpoint, access_key, secret_key, bucket, region, path_style, base_url)


def get_url(storage_type: str, bucket: str, file_key: str) -> str:
    cfg = get_config()
    eng = get_storage(storage_type)
    if eng is None:
        return ""
    url = eng.get_url(bucket, file_key)
    if url.startswith("/") and cfg.default_base_url:
        return cfg.default_base_url + url
    return url
