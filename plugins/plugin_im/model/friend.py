"""Friend request and friendship ORM models."""

from __future__ import annotations

from datetime import datetime
from typing import Optional
from sqlalchemy import String, Text, DateTime, Index, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column
from core.plugin.registry import HeiBase


class FriendRequest(HeiBase):
    """好友请求"""
    __tablename__ = "im_friend_request"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    sender_id: Mapped[str] = mapped_column(String(32), nullable=False, index=True)
    sender_type: Mapped[str] = mapped_column(String(20), nullable=False)
    receiver_id: Mapped[str] = mapped_column(String(32), nullable=False, index=True)
    receiver_type: Mapped[str] = mapped_column(String(20), nullable=False)
    remark: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    status: Mapped[str] = mapped_column(String(10), nullable=False, default="pending")  # pending | accepted | rejected
    created_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    updated_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)

    __table_args__ = (
        Index("idx_fr_sender_status", "sender_id", "status"),
        Index("idx_fr_receiver_status", "receiver_id", "status"),
    )


class Friendship(HeiBase):
    """好友关系（双向各存一条）"""
    __tablename__ = "im_friendship"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    user_id: Mapped[str] = mapped_column(String(32), nullable=False)
    user_type: Mapped[str] = mapped_column(String(20), nullable=False)
    friend_id: Mapped[str] = mapped_column(String(32), nullable=False)
    friend_type: Mapped[str] = mapped_column(String(20), nullable=False)
    remark: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    created_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)

    __table_args__ = (
        UniqueConstraint("user_id", "user_type", "friend_id", "friend_type", name="uq_fs_pair"),
    )


class FriendBlock(HeiBase):
    """黑名单"""
    __tablename__ = "im_friend_block"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    user_id: Mapped[str] = mapped_column(String(32), nullable=False)
    user_type: Mapped[str] = mapped_column(String(20), nullable=False)
    blocked_id: Mapped[str] = mapped_column(String(32), nullable=False)
    blocked_type: Mapped[str] = mapped_column(String(20), nullable=False)
    created_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)

    __table_args__ = (
        UniqueConstraint("user_id", "user_type", "blocked_id", "blocked_type", name="uq_fb_pair"),
    )
