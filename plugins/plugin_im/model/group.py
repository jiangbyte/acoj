"""Group chat ORM models."""

from __future__ import annotations

from datetime import datetime
from typing import Optional
from sqlalchemy import String, Text, Boolean, Integer, DateTime, UniqueConstraint, Index, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column
from core.plugin.registry import HeiBase


class Group(HeiBase):
    """群组"""
    __tablename__ = "im_group"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    avatar: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    owner_id: Mapped[str] = mapped_column(String(32), nullable=False, index=True)
    owner_type: Mapped[str] = mapped_column(String(20), nullable=False)
    group_type: Mapped[str] = mapped_column(String(20), nullable=False, default="mixed")
    notice: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    is_public: Mapped[bool] = mapped_column(Boolean, default=False)
    max_members: Mapped[int] = mapped_column(Integer, default=200)
    status: Mapped[str] = mapped_column(String(10), nullable=False, default="normal")  # normal | dissolved
    created_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    updated_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)


class GroupMember(HeiBase):
    """群成员"""
    __tablename__ = "im_group_member"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    group_id: Mapped[str] = mapped_column(String(32), nullable=False, index=True)
    user_id: Mapped[str] = mapped_column(String(32), nullable=False)
    user_type: Mapped[str] = mapped_column(String(20), nullable=False)
    role: Mapped[str] = mapped_column(String(20), nullable=False, default="member")  # owner | admin | member
    nickname: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    is_muted: Mapped[bool] = mapped_column(Boolean, default=False)
    muted_until: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    joined_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    status: Mapped[str] = mapped_column(String(10), nullable=False, default="active")  # active | left | kicked

    __table_args__ = (
        UniqueConstraint("group_id", "user_id", "user_type", name="uq_gm_member"),
    )


class GroupJoinRequest(HeiBase):
    """加群请求"""
    __tablename__ = "im_group_join_request"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    group_id: Mapped[str] = mapped_column(String(32), nullable=False, index=True)
    user_id: Mapped[str] = mapped_column(String(32), nullable=False)
    user_type: Mapped[str] = mapped_column(String(20), nullable=False)
    remark: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    status: Mapped[str] = mapped_column(String(10), nullable=False, default="pending")  # pending | approved | rejected
    created_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    updated_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)


class GroupMessage(HeiBase):
    """群消息"""
    __tablename__ = "im_group_message"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    group_id: Mapped[str] = mapped_column(String(32), nullable=False, index=True)
    sender_id: Mapped[str] = mapped_column(String(32), nullable=False, index=True)
    sender_type: Mapped[str] = mapped_column(String(20), nullable=False)
    content: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    extra: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    msg_type: Mapped[str] = mapped_column(String(20), default="TEXT")  # TEXT | IMAGE | FILE | SYSTEM
    reply_to: Mapped[Optional[str]] = mapped_column(String(32), nullable=True)
    created_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)

    __table_args__ = (
        Index("idx_gm_group_time", "group_id", "created_at"),
    )


class GroupMessageRead(HeiBase):
    """群消息已读记录"""
    __tablename__ = "im_group_message_read"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    group_id: Mapped[str] = mapped_column(String(32), nullable=False, index=True)
    user_id: Mapped[str] = mapped_column(String(32), nullable=False)
    user_type: Mapped[str] = mapped_column(String(20), nullable=False)
    read_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)

    __table_args__ = (
        UniqueConstraint("group_id", "user_id", "user_type", name="uq_gmr_read"),
    )
