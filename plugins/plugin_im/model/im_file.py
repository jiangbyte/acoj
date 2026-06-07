"""IM file upload ORM model."""

from __future__ import annotations

from datetime import datetime
from typing import Optional
from sqlalchemy import String, Text, Integer, DateTime, BigInteger
from sqlalchemy.orm import Mapped, mapped_column
from core.plugin.registry import HeiBase


class ImFile(HeiBase):
    """IM模块上传文件记录"""
    __tablename__ = "im_file"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    engine: Mapped[str] = mapped_column(String(32), nullable=False)           # LOCAL, MINIO, S3
    bucket: Mapped[str] = mapped_column(String(128), nullable=False)
    file_key: Mapped[str] = mapped_column(String(500), nullable=False, index=True)
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    suffix: Mapped[Optional[str]] = mapped_column(String(32), nullable=True)
    size_kb: Mapped[int] = mapped_column(BigInteger, default=0)
    size_info: Mapped[Optional[str]] = mapped_column(String(32), nullable=True)
    storage_path: Mapped[Optional[str]] = mapped_column(String(500), nullable=True)
    download_path: Mapped[Optional[str]] = mapped_column(String(500), nullable=True)
    thumbnail: Mapped[Optional[str]] = mapped_column(String(500), nullable=True)
    checksum: Mapped[Optional[str]] = mapped_column(String(128), nullable=True)
    checksum_algo: Mapped[Optional[str]] = mapped_column(String(16), nullable=True)
    conversation_id: Mapped[Optional[str]] = mapped_column(String(32), index=True, nullable=True)
    sender_id: Mapped[str] = mapped_column(String(32), nullable=False, index=True)
    sender_type: Mapped[str] = mapped_column(String(20), nullable=False)
    msg_type: Mapped[str] = mapped_column(String(20), nullable=False)  # IMAGE | FILE
    created_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
