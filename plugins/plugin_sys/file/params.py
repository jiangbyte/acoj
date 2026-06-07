"""File module parameters — mirrors hei-gin plugins/plugin-sys/file/params.go."""

from __future__ import annotations

from pydantic import BaseModel, Field
from typing import Optional


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
