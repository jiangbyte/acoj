"""Storage registry and factory for sdk storage backends."""

from __future__ import annotations

from dataclasses import dataclass
from typing import Callable

from sdk.config.settings import settings

from .interface import ChunkInfo, ChunkedUploader, FileStorageInterface
from .local_storage import LocalStorage
from .minio_storage import MinioStorage
from .s3_storage import S3Storage

__all__ = [
    "FileStorageInterface",
    "ChunkInfo",
    "ChunkedUploader",
    "StorageBackendDefinition",
    "StorageRegistrySnapshot",
    "register_storage_backend",
    "get_storage",
    "get_default_storage",
    "get_storage_url",
    "get_url",
    "storage_snapshot",
    "LocalStorage",
    "MinioStorage",
    "S3Storage",
]


StorageBuilder = Callable[[], FileStorageInterface | None]


@dataclass(slots=True)
class StorageBackendDefinition:
    name: str
    builder: StorageBuilder


@dataclass(slots=True)
class StorageRegistrySnapshot:
    default: str
    registered: list[str]


class StorageRegistry:
    def __init__(self) -> None:
        self._builders: dict[str, StorageBuilder] = {}

    def register(self, name: str, builder: StorageBuilder) -> None:
        key = str(name or "").upper()
        if not key:
            raise ValueError("storage backend name is required")
        self._builders[key] = builder

    def get(self, name: str) -> FileStorageInterface | None:
        builder = self._builders.get(str(name or "").upper())
        if builder is None:
            return None
        return builder()

    def snapshot(self) -> StorageRegistrySnapshot:
        return StorageRegistrySnapshot(
            default=str(settings.storage.default or "").upper(),
            registered=sorted(self._builders),
        )


_registry = StorageRegistry()


def _build_local() -> FileStorageInterface:
    cfg = settings.storage.local
    return LocalStorage(cfg.upload_folder, cfg.base_url)


def _build_minio() -> FileStorageInterface | None:
    cfg = settings.storage.minio
    if not cfg.endpoint:
        return None
    return MinioStorage(
        endpoint=cfg.endpoint,
        access_key=cfg.access_key,
        secret_key=cfg.secret_key,
        default_bucket=cfg.bucket,
        secure=cfg.secure,
        region=cfg.region,
        base_url=cfg.base_url,
    )


def _build_s3() -> FileStorageInterface | None:
    cfg = settings.storage.s3
    if not cfg.endpoint:
        return None
    return S3Storage(
        endpoint=cfg.endpoint,
        access_key=cfg.access_key,
        secret_key=cfg.secret_key,
        default_bucket=cfg.bucket,
        region=cfg.region,
        path_style=cfg.path_style,
        base_url=cfg.base_url,
    )


def register_storage_backend(name: str, builder: StorageBuilder) -> None:
    _registry.register(name, builder)


def get_storage(storage_type: str) -> FileStorageInterface | None:
    resolved = _registry.get(storage_type)
    if resolved is not None:
        return resolved
    if str(storage_type or "").upper() == "LOCAL":
        return _build_local()
    return None


def get_default_storage() -> FileStorageInterface:
    resolved = get_storage(settings.storage.default)
    if resolved is not None:
        return resolved
    return _build_local()


def get_storage_url(storage_type: str, bucket: str, file_key: str) -> str:
    engine = get_storage(storage_type)
    if engine is None:
        return ""
    url = engine.get_url(bucket, file_key)
    if url.startswith("/") and settings.storage.default_base_url:
        return settings.storage.default_base_url.rstrip("/") + url
    return url


def get_url(storage_type: str, bucket: str, file_key: str) -> str:
    return get_storage_url(storage_type, bucket, file_key)


def storage_snapshot() -> StorageRegistrySnapshot:
    return _registry.snapshot()


register_storage_backend("LOCAL", _build_local)
register_storage_backend("MINIO", _build_minio)
register_storage_backend("S3", _build_s3)
