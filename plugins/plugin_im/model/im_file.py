"""IM file upload ORM model and VO."""

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

    def to_vo(self) -> ImFileVO:
        """Convert to view object."""
        created_at_str = ""
        if self.created_at:
            created_at_str = self.created_at.strftime("%Y-%m-%d %H:%M:%S")
        return ImFileVO(
            id=self.id or "",
            engine=self.engine or "",
            bucket=self.bucket or "",
            file_key=self.file_key or "",
            name=self.name or "",
            suffix=self.suffix or "",
            size_kb=self.size_kb or 0,
            size_info=self.size_info or "",
            download_path=self.download_path or "",
            thumbnail=self.thumbnail or "",
            conversation_id=self.conversation_id or "",
            sender_id=self.sender_id or "",
            sender_type=self.sender_type or "",
            msg_type=self.msg_type or "",
            created_at=created_at_str,
        )


class ImFileVO:
    """IM file view object."""
    def __init__(
        self,
        id: str = "",
        engine: str = "",
        bucket: str = "",
        file_key: str = "",
        name: str = "",
        suffix: str = "",
        size_kb: int = 0,
        size_info: str = "",
        download_path: str = "",
        thumbnail: str = "",
        conversation_id: str = "",
        sender_id: str = "",
        sender_type: str = "",
        msg_type: str = "",
        created_at: str = "",
    ):
        self.id = id
        self.engine = engine
        self.bucket = bucket
        self.file_key = file_key
        self.name = name
        self.suffix = suffix
        self.size_kb = size_kb
        self.size_info = size_info
        self.download_path = download_path
        self.thumbnail = thumbnail
        self.conversation_id = conversation_id
        self.sender_id = sender_id
        self.sender_type = sender_type
        self.msg_type = msg_type
        self.created_at = created_at
