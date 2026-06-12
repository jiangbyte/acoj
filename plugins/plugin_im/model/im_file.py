"""IM file upload ORM model and VO."""

from __future__ import annotations

from datetime import datetime
from typing import Optional
from sqlalchemy import Index, text
from sqlalchemy.dialects.mysql import BIGINT, DATETIME, VARCHAR
from sqlalchemy.orm import Mapped, mapped_column
from core.plugin.registry import HeiBase


class ImFile(HeiBase):
    """IM模块上传文件记录"""
    __tablename__ = "im_file"
    __table_args__ = (
        Index("idx_file_key", "file_key"),
        Index("idx_conversation_id", "conversation_id"),
        Index("idx_sender_id", "sender_id"),
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), primary_key=True)
    engine: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    bucket: Mapped[str] = mapped_column(VARCHAR(128, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    file_key: Mapped[str] = mapped_column(VARCHAR(500, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    name: Mapped[str] = mapped_column(VARCHAR(255, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    suffix: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), default="", server_default=text("''"))
    size_kb: Mapped[int] = mapped_column(BIGINT, default=0, server_default=text("0"))
    size_info: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), default="", server_default=text("''"))
    storage_path: Mapped[str] = mapped_column(VARCHAR(500, charset="utf8mb4", collation="utf8mb4_general_ci"), default="", server_default=text("''"))
    download_path: Mapped[str] = mapped_column(VARCHAR(500, charset="utf8mb4", collation="utf8mb4_general_ci"), default="", server_default=text("''"))
    thumbnail: Mapped[str] = mapped_column(VARCHAR(500, charset="utf8mb4", collation="utf8mb4_general_ci"), default="", server_default=text("''"))
    checksum: Mapped[str] = mapped_column(VARCHAR(128, charset="utf8mb4", collation="utf8mb4_general_ci"), default="", server_default=text("''"))
    checksum_algo: Mapped[str] = mapped_column(VARCHAR(16, charset="utf8mb4", collation="utf8mb4_general_ci"), default="", server_default=text("''"))
    conversation_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), default="", server_default=text("''"))
    sender_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    sender_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    msg_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    created_at: Mapped[datetime] = mapped_column(DATETIME, nullable=False)

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
