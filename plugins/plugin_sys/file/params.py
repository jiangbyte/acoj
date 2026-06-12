"""File module parameters — mirrors hei-gin plugins/plugin-sys/file/params.go."""

from __future__ import annotations

from pydantic import BaseModel, Field
from typing import Optional
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


class FileVO(BaseModel):
    """Full file VO — matches hei-gin's FileVO for page/detail responses."""
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
    created_at: str = ""
    created_by: str = ""
    updated_at: str = ""
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


def SysFileToFileUploadResult(src: Optional[SysFile]) -> Optional[FileUploadResult]:
    if src is None:
        return None
    return FileUploadResult(
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


def SysFileToFileVO(src: Optional[SysFile]) -> Optional[FileVO]:
    if src is None:
        return None
    return FileVO(
        id=src.id,
        engine=src.engine,
        bucket=src.bucket,
        file_key=src.file_key,
        name=src.name,
        suffix=src.suffix or "",
        size_kb=src.size_kb,
        size_info=src.size_info or "",
        obj_name=src.obj_name or "",
        storage_path=src.storage_path or "",
        download_path=src.download_path or "",
        is_download_auth=src.is_download_auth or False,
        thumbnail=src.thumbnail or "",
        checksum=src.checksum or "",
        checksum_algo=src.checksum_algo or "",
        ext_json=src.ext_json or "",
        created_at=src.created_at.strftime("%Y-%m-%d %H:%M:%S") if src.created_at else "",
        created_by=src.created_by or "",
        updated_at=src.updated_at.strftime("%Y-%m-%d %H:%M:%S") if src.updated_at else "",
        updated_by=src.updated_by or "",
    )
