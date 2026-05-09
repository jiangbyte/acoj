from .interface import FileStorageInterface

__all__ = ["FileStorageInterface", "LocalStorage", "MinioStorage", "S3Storage"]


def LocalStorage(*args, **kwargs):
    from .local_storage import LocalStorage as _cls
    return _cls(*args, **kwargs)


def MinioStorage(*args, **kwargs):
    from .minio_storage import MinioStorage as _cls
    return _cls(*args, **kwargs)


def S3Storage(*args, **kwargs):
    from .s3_storage import S3Storage as _cls
    return _cls(*args, **kwargs)
