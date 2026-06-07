"""SysFile ORM model — mirrors hei-gin plugins/plugin-sys/file/model.go."""

from __future__ import annotations

from datetime import datetime
from typing import Optional
from sqlalchemy import String, Text, BigInteger, Boolean, DateTime
from sqlalchemy.orm import Mapped, mapped_column
from core.plugin.registry import HeiBase


class SysFile(HeiBase):
    """系统文件存储记录（与IM文件共用存储引擎，但独立表）"""
    __tablename__ = "sys_file"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    engine: Mapped[str] = mapped_column(String(32), nullable=False)          # LOCAL, MINIO, S3
    bucket: Mapped[str] = mapped_column(String(128), nullable=False)         # storage bucket
    file_key: Mapped[str] = mapped_column(String(500), nullable=False, unique=True, index=True)
    name: Mapped[str] = mapped_column(String(255), nullable=False)           # original filename
    suffix: Mapped[Optional[str]] = mapped_column(String(32), nullable=True) # file extension
    size_kb: Mapped[int] = mapped_column(BigInteger, default=0)
    size_info: Mapped[Optional[str]] = mapped_column(String(32), nullable=True)
    obj_name: Mapped[Optional[str]] = mapped_column(String(500), nullable=True)
    storage_path: Mapped[Optional[str]] = mapped_column(String(500), nullable=True)
    download_path: Mapped[Optional[str]] = mapped_column(String(500), nullable=True)
    is_download_auth: Mapped[bool] = mapped_column(Boolean, default=False)
    thumbnail: Mapped[Optional[str]] = mapped_column(String(500), nullable=True)
    checksum: Mapped[Optional[str]] = mapped_column(String(128), nullable=True)
    checksum_algo: Mapped[Optional[str]] = mapped_column(String(16), nullable=True)
    ext_json: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, nullable=True)
    created_by: Mapped[Optional[str]] = mapped_column(String(32), nullable=True)
    updated_at: Mapped[datetime] = mapped_column(DateTime, nullable=True)
    updated_by: Mapped[Optional[str]] = mapped_column(String(32), nullable=True)
