from app.core.config.settings import settings
from app.platform.storage.local import LocalStorage
from app.platform.storage.s3 import S3Storage


def get_storage():
    """按配置返回当前存储实现，默认优先支持 S3/MinIO，对原型场景保留本地存储回退。"""
    if settings.storage.provider == "local":
        return LocalStorage(settings.storage.local_root)
    return S3Storage()
