from app.core.config.settings import settings
from app.platform.storage.local import LocalStorage
from app.platform.storage.oss import OSSStorage
from app.platform.storage.s3 import MinioStorage, S3Storage


def get_storage():
    """按配置返回当前存储实现。"""
    provider = settings.storage.provider.lower()
    if provider == "local":
        return LocalStorage(settings.storage.local_root)
    if provider == "minio":
        return MinioStorage()
    if provider == "s3":
        return S3Storage()
    if provider == "oss":
        return OSSStorage()
    raise ValueError(f"Unsupported storage provider: {settings.storage.provider}")
