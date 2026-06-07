"""
Storage abstraction — mirrors hei-gin's ``sdk/storage/``.

Provides:
* ``FileStorageInterface`` — abstract base (mirrors ``Engine``)
* ``get_storage(type_name)`` — factory (mirrors ``GetStorage()``)
* ``get_default_storage()`` — reads from config (mirrors factory.go)
* ``ChunkedUploader`` — chunked upload support (mirrors chunk.go)
"""

from __future__ import annotations

from typing import Optional
from config.settings import settings

from .interface import FileStorageInterface
from .interface import ChunkedUploader  # re-export

__all__ = [
    "FileStorageInterface", "ChunkedUploader",
    "get_storage", "get_default_storage",
    "LocalStorage", "MinioStorage", "S3Storage",
]


def get_storage(storage_type: str) -> Optional[FileStorageInterface]:
    """Factory: return a storage backend by type name.

    Supported types: ``"LOCAL"``, ``"MINIO"``, ``"S3"``.
    Falls back to ``LocalStorage`` for unknown types.

    Mirrors hei-gin's ``storage.GetStorage(storageType string) Engine``.
    """
    cfg = settings.storage
    if storage_type == "LOCAL":
        return LocalStorage(cfg.local.upload_folder, cfg.local.base_url)
    elif storage_type == "MINIO":
        m = cfg.minio
        if not m.endpoint:
            return None
        return MinioStorage(m.endpoint, m.access_key, m.secret_key,
                            m.bucket, m.secure, m.region, m.base_url)
    elif storage_type == "S3":
        s = cfg.s3
        if not s.endpoint:
            return None
        return S3Storage(s.endpoint, s.access_key, s.secret_key,
                         s.bucket, s.region, s.path_style, s.base_url)
    return LocalStorage(cfg.local.upload_folder, cfg.local.base_url)


def get_default_storage() -> FileStorageInterface:
    """Return the default storage backend as configured.

    Mirrors hei-gin's pattern of using ``GetStorage(GetConfig().Default)``.
    """
    result = get_storage(settings.storage.default)
    if result is None:
        # Fallback to local
        return LocalStorage(settings.storage.local.upload_folder,
                           settings.storage.local.base_url)
    return result


def LocalStorage(upload_folder: str = "./uploads", base_url: str = ""):
    from .local_storage import LocalStorage as _cls
    return _cls(upload_folder, base_url)


def MinioStorage(endpoint: str, access_key: str, secret_key: str,
                 bucket: str = "hei", secure: bool = False,
                 region: str = "us-east-1", base_url: str = ""):
    from .minio_storage import MinioStorage as _cls
    return _cls(endpoint, access_key, secret_key, bucket, secure, region, base_url)


def S3Storage(endpoint: str, access_key: str, secret_key: str,
              bucket: str = "hei", region: str = "us-east-1",
              path_style: bool = True, base_url: str = ""):
    from .s3_storage import S3Storage as _cls
    return _cls(endpoint, access_key, secret_key, bucket, region, path_style, base_url)


def get_url(storage_type: str, bucket: str, file_key: str) -> str:
    """Resolve a file URL from storage type, bucket, and key.

    Mirrors hei-gin's ``storage.GetURL()``.
    """
    eng = get_storage(storage_type)
    if eng is None:
        return ""
    return eng.get_url(bucket, file_key)
