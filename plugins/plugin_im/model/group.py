"""Group chat ORM models."""

from __future__ import annotations

from datetime import datetime
from typing import Optional
from sqlalchemy import Index, text
from sqlalchemy.dialects.mysql import BOOLEAN, DATETIME, INTEGER, TEXT, VARCHAR
from sqlalchemy.orm import Mapped, mapped_column
from sdk.kernel.registry import HeiBase


class Group(HeiBase):
    """群组"""
    __tablename__ = "im_group"
    __table_args__ = (
        Index("idx_owner_id", "owner_id"),
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), primary_key=True)
    name: Mapped[str] = mapped_column(VARCHAR(100, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    avatar: Mapped[str] = mapped_column(VARCHAR(255, charset="utf8mb4", collation="utf8mb4_general_ci"), default="", server_default=text("''"))
    owner_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    owner_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    group_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False, default="mixed", server_default=text("'mixed'"))
    notice: Mapped[str] = mapped_column(TEXT(collation="utf8mb4_general_ci"), default="", server_default=text("''"))
    is_public: Mapped[bool] = mapped_column(BOOLEAN, default=False, server_default=text("false"))
    max_members: Mapped[int] = mapped_column(INTEGER, default=200, server_default=text("200"))
    status: Mapped[str] = mapped_column(VARCHAR(10, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False, default="normal", server_default=text("'normal'"))
    created_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)
    updated_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)


class GroupMember(HeiBase):
    """群成员"""
    __tablename__ = "im_group_member"
    __table_args__ = (
        Index("idx_gm_group_user", "group_id", "user_id", "user_type", unique=True),
        Index("idx_group_id", "group_id"),
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), primary_key=True)
    group_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    user_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    user_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    role: Mapped[str] = mapped_column(VARCHAR(10, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False, default="member", server_default=text("'member'"))
    nickname: Mapped[str] = mapped_column(VARCHAR(100, charset="utf8mb4", collation="utf8mb4_general_ci"), default="", server_default=text("''"))
    muted_until: Mapped[Optional[datetime]] = mapped_column(DATETIME)
    joined_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)
    status: Mapped[str] = mapped_column(VARCHAR(10, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False, default="active", server_default=text("'active'"))


class GroupJoinRequest(HeiBase):
    """加群请求"""
    __tablename__ = "im_group_join_request"
    __table_args__ = (
        Index("idx_group_id", "group_id"),
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), primary_key=True)
    group_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    user_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    user_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    reason: Mapped[str] = mapped_column(VARCHAR(255, charset="utf8mb4", collation="utf8mb4_general_ci"), default="", server_default=text("''"))
    status: Mapped[str] = mapped_column(VARCHAR(10, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False, default="pending", server_default=text("'pending'"))
    handled_by: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), default="", server_default=text("''"))
    created_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)
    updated_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)


class GroupMessage(HeiBase):
    """群消息"""
    __tablename__ = "im_group_message"
    __table_args__ = (
        Index("idx_gmsg_group_created", "group_id", "created_at"),
        Index("idx_sender_id", "sender_id"),
        Index("idx_reply_to", "reply_to"),
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), primary_key=True)
    group_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    sender_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    sender_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    content: Mapped[str] = mapped_column(TEXT(collation="utf8mb4_general_ci"))
    extra: Mapped[str] = mapped_column(TEXT(collation="utf8mb4_general_ci"))
    msg_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False, default="text", server_default=text("'text'"))
    reply_to: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), default="", server_default=text("''"))
    created_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)


class GroupMessageRead(HeiBase):
    """群消息已读记录"""
    __tablename__ = "im_group_message_read"
    __table_args__ = (
        Index("idx_gmr_msg_user", "message_id", "user_id", "user_type", unique=True),
        Index("idx_group_id", "group_id"),
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), primary_key=True)
    message_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    group_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    user_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    user_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    read_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)
