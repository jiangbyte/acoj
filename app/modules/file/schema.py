from datetime import datetime

from pydantic import Field

from app.core.schema.base import ApiSchema


class SysFileSchema(ApiSchema):
    id: str
    object_name: str
    original_name: str
    storage_provider: str
    bucket: str | None = None
    content_type: str
    size: int
    url: str
    created_at: datetime = Field(examples=["2026-06-17T12:00:00Z"])
    created_by: str | None = None
    updated_at: datetime = Field(examples=["2026-06-17T12:00:00Z"])
    updated_by: str | None = None


class FileUploadRequest(ApiSchema):
    """文件上传请求载荷，封装上传原文件信息和内容类型。"""

    filename: str
    content: bytes
    content_type: str


class FileRecordCreate(ApiSchema):
    """文件元数据创建载荷，统一仓储层落库参数。"""

    object_name: str
    original_name: str
    storage_provider: str
    bucket: str | None = None
    content_type: str
    size: int
    url: str


class FileUrlRequest(ApiSchema):
    object_name: str = Field(min_length=1, max_length=255)


class FileUrlResponse(ApiSchema):
    object_name: str
    url: str


class FileDeleteResponse(ApiSchema):
    object_name: str
    deleted: bool = True
