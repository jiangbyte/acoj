"""IM file upload ORM model."""

from __future__ import annotations

from datetime import datetime
from sqlalchemy import Index, text
from sqlalchemy.dialects.mysql import BIGINT, DATETIME, VARCHAR
from sqlalchemy.orm import Mapped, mapped_column
from sdk.kernel.registry import HeiBase


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
