"""File module parameters — mirrors hei-gin plugins/plugin-sys/file/params.go."""

from __future__ import annotations

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field

from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin

from .models import SysFile


class FilePageParam(BaseModel):
    current: int = Field(1, ge=1)
    size: int = Field(10, ge=1, le=100)
    keyword: str = ""
    engine: str = ""
    bucket: str = ""


class FileUploadResult(BaseModel):
    id: str = ""
    engine: str = ""
    bucket: str = ""
    file_key: str = ""
    original_name: str = ""
    file_suffix: str = ""
    file_size_kb: int = 0
    size_info: str = ""
    download_path: str = ""
    thumbnail: str = ""

    @classmethod
    def from_entity(cls, src: Optional[SysFile]) -> Optional["FileUploadResult"]:
        if src is None:
            return None
        return cls(
            id=src.id,
            engine=src.engine,
            bucket=src.bucket,
            file_key=src.file_key,
            original_name=src.name,
            file_suffix=src.suffix or "",
            file_size_kb=src.size_kb,
            size_info=src.size_info or "",
            download_path=src.download_path or "",
            thumbnail=src.thumbnail or "",
        )


class FileVO(DateTimeValidatorMixin, BaseModel):
    """Full file VO — matches hei-gin's FileVO for page/detail responses."""
    model_config = ConfigDict(from_attributes=True)

    id: str = ""
    engine: str = ""
    bucket: str = ""
    file_key: str = ""
    name: str = ""
    suffix: str = ""
    size_kb: int = 0
    size_info: str = ""
    obj_name: str = ""
    storage_path: str = ""
    download_path: str = ""
    is_download_auth: bool = False
    thumbnail: str = ""
    checksum: str = ""
    checksum_algo: str = ""
    ext_json: str = ""
    created_at: Optional[datetime] = None
    created_by: str = ""
    updated_at: Optional[datetime] = None
    updated_by: str = ""


class ChunkUploadInitParam(BaseModel):
    file_name: str = Field(..., description="文件名")
    file_size: int = Field(..., description="文件大小")
    total_chunks: int = Field(..., description="总分片数")
    engine: str = Field("LOCAL", description="存储引擎")
    bucket: str = Field("DEFAULT", description="存储桶")


class ChunkUploadPartParam(BaseModel):
    upload_id: str = Field(..., description="上传ID")
    chunk_index: int = Field(..., description="分片索引(0-based)")
    total_chunks: int = Field(..., description="总分片数")
    checksum: str = Field("", description="分片SHA256")
    engine: str = Field("LOCAL", description="存储引擎")
    bucket: str = Field("DEFAULT", description="存储桶")
    file_key: str = Field("", description="文件Key")


class ChunkCompleteParam(BaseModel):
    upload_id: str = Field(..., description="上传ID")
    name: str = Field(..., description="原文件名")
    file_key: str = Field(..., description="文件Key")
    file_size: int = Field(..., description="文件大小")
    engine: str = Field("LOCAL", description="存储引擎")
    bucket: str = Field("DEFAULT", description="存储桶")
    total_chunks: int = Field(..., description="总分片数")


class ChunkAbortParam(BaseModel):
    upload_id: str = Field(..., description="上传ID")
    engine: str = Field("LOCAL", description="存储引擎")
    bucket: str = Field("DEFAULT", description="存储桶")
    file_key: str = Field("", description="文件Key")
