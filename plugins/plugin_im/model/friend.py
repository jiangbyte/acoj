"""Friend request and friendship ORM models."""

from __future__ import annotations

from datetime import datetime
from typing import Optional
from sqlalchemy import Index, text
from sqlalchemy.dialects.mysql import DATETIME, VARCHAR
from sqlalchemy.orm import Mapped, mapped_column
from core.plugin.registry import HeiBase


class FriendRequest(HeiBase):
    """好友请求"""
    __tablename__ = "im_friend_request"
    __table_args__ = (
        Index("idx_fr_sender_status", "sender_id", "status"),
        Index("idx_fr_receiver_status", "receiver_id", "status"),
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), primary_key=True)
    sender_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    sender_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    receiver_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    receiver_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    remark: Mapped[str] = mapped_column(VARCHAR(255, charset="utf8mb4", collation="utf8mb4_general_ci"), default="", server_default=text("''"))
    status: Mapped[str] = mapped_column(VARCHAR(10, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False, default="pending", server_default=text("'pending'"))
    created_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)
    updated_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)


class Friendship(HeiBase):
    """好友关系（双向各存一条）"""
    __tablename__ = "im_friendship"
    __table_args__ = (
        Index("idx_fs_pair", "user_id", "user_type", "friend_id", "friend_type", unique=True),
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), primary_key=True)
    user_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    user_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    friend_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    friend_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    remark: Mapped[str] = mapped_column(VARCHAR(100, charset="utf8mb4", collation="utf8mb4_general_ci"), default="", server_default=text("''"))
    created_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)


class FriendBlock(HeiBase):
    """黑名单"""
    __tablename__ = "im_friend_block"
    __table_args__ = (
        Index("idx_fb_pair", "user_id", "user_type", "blocked_id", "blocked_type", unique=True),
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), primary_key=True)
    user_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    user_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    blocked_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    blocked_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    created_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)
