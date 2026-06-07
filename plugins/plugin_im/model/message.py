"""Message and conversation ORM models + helper types."""

from __future__ import annotations

import hashlib
from datetime import datetime
from typing import Optional
from sqlalchemy import String, Text, Integer, DateTime, Index, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column
from core.plugin.registry import HeiBase
from core.enums import LoginTypeEnum


# Message type constants
MsgTypeText = "TEXT"
MsgTypeImage = "IMAGE"
MsgTypeFile = "FILE"
MsgTypeSystem = "SYSTEM"

# Conversation types
ConvTypeSingle = "single"
ConvTypeGroup = "group"


class Message(HeiBase):
    """统一单聊消息表"""
    __tablename__ = "im_message"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    conversation_id: Mapped[str] = mapped_column(String(32), nullable=False, index=True)
    content: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    extra: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    msg_type: Mapped[str] = mapped_column(String(20), default="TEXT")
    sender_id: Mapped[str] = mapped_column(String(32), index=True, nullable=True)
    sender_type: Mapped[str] = mapped_column(String(20), nullable=True)
    receiver_id: Mapped[str] = mapped_column(String(32), index=True, nullable=True)
    receiver_type: Mapped[str] = mapped_column(String(20), nullable=True)
    status: Mapped[str] = mapped_column(String(10), nullable=False, default="unread")  # unread | read
    deleted_by: Mapped[Optional[str]] = mapped_column(String(32), nullable=True)
    read_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    created_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    updated_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)

    __table_args__ = (
        Index("idx_msg_conv_time", "conversation_id", "created_at"),
    )


class Conversation(HeiBase):
    """会话缓存表"""
    __tablename__ = "im_conversation"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    from_id: Mapped[str] = mapped_column("from_id", String(32), nullable=False)
    from_type: Mapped[str] = mapped_column("from_type", String(20), nullable=False)
    to_id: Mapped[str] = mapped_column("to_id", String(32), nullable=False)
    to_type: Mapped[str] = mapped_column("to_type", String(20), nullable=False)
    last_msg: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    last_time: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    created_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    updated_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)


class ConversationUnread(HeiBase):
    """每用户每会话未读数"""
    __tablename__ = "im_conversation_unread"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    conversation_id: Mapped[str] = mapped_column(String(32), nullable=False)
    user_id: Mapped[str] = mapped_column(String(32), nullable=False)
    user_type: Mapped[str] = mapped_column(String(20), nullable=False)
    unread_count: Mapped[int] = mapped_column(Integer, default=0)

    __table_args__ = (
        UniqueConstraint("conversation_id", "user_id", "user_type", name="uq_conv_unread"),
    )


# ── Helper data classes (not ORM models) ──────────────────────────────

class MsgExtraImage:
    """Extra metadata for IMAGE messages."""
    def __init__(self, w: int = 0, h: int = 0, format: str = "", thumbnail: str = ""):
        self.w = w
        self.h = h
        self.format = format
        self.thumbnail = thumbnail


class MsgExtraFile:
    """Extra metadata for FILE messages."""
    def __init__(self, name: str = "", size: int = 0, mime: str = ""):
        self.name = name
        self.size = size
        self.mime = mime


class MsgExtraSystem:
    """Extra metadata for SYSTEM messages."""
    def __init__(self, action: str = "", operator_id: str = "",
                 user_id: str = "", user_type: str = ""):
        self.action = action
        self.operator_id = operator_id or ""
        self.user_id = user_id or ""
        self.user_type = user_type or ""


def generate_conversation_id(
    u1_id: str, u1_type: LoginTypeEnum,
    u2_id: str, u2_type: LoginTypeEnum,
) -> str:
    """Generate a deterministic conversation ID from two user identifiers.

    Mirrors Go:  sha1.Sum -> [:8] -> fmt.Sprintf("%x")
    """
    key1 = f"{u1_type.value}:{u1_id}"
    key2 = f"{u2_type.value}:{u2_id}"
    if key1 > key2:
        key1, key2 = key2, key1
    h = hashlib.sha1(f"{key1}|{key2}".encode()).digest()
    return h[:8].hex()
