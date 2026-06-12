"""Message and conversation ORM models + helper types."""

from __future__ import annotations

import hashlib
from datetime import datetime
from typing import Optional
from sqlalchemy import Index, text
from sqlalchemy.dialects.mysql import DATETIME, INTEGER, TEXT, VARCHAR
from sqlalchemy.orm import Mapped, mapped_column
from sdk.kernel.registry import HeiBase
from sdk.enums import LoginTypeEnum


# Message type constants
MsgTypeText = "TEXT"
MsgTypeImage = "IMAGE"
MsgTypeFile = "FILE"
MsgTypeSystem = "SYSTEM"

# Conversation types
ConvTypeSingle = "single"
ConvTypeGroup = "group"


class Message(HeiBase):
    """Unified single-chat message table."""
    __tablename__ = "im_message"
    __table_args__ = (
        Index("idx_conversation_id", "conversation_id"),
        Index("idx_msg_conv_created", "conversation_id", "created_at"),
        Index("idx_sender_id", "sender_id"),
        Index("idx_msg_sender_type_created", "sender_id", "sender_type", "created_at"),
        Index("idx_receiver_id", "receiver_id"),
        Index("idx_msg_receiver_type_status", "receiver_id", "receiver_type", "status"),
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), primary_key=True)
    conversation_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    content: Mapped[str] = mapped_column(TEXT(collation="utf8mb4_general_ci"))
    extra: Mapped[str] = mapped_column(TEXT(collation="utf8mb4_general_ci"))
    msg_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), default="TEXT", server_default=text("'TEXT'"))
    sender_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"))
    sender_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"))
    receiver_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"))
    receiver_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"))
    status: Mapped[str] = mapped_column(VARCHAR(10, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False, default="unread", server_default=text("'unread'"))
    deleted_by: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), default="", server_default=text("''"))
    read_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)
    created_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)
    updated_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)


class Conversation(HeiBase):
    """Conversation metadata cache table."""
    __tablename__ = "im_conversation"

    id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), primary_key=True)
    from_id: Mapped[str] = mapped_column("from_id", VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    from_type: Mapped[str] = mapped_column("from_type", VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    to_id: Mapped[str] = mapped_column("to_id", VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    to_type: Mapped[str] = mapped_column("to_type", VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    last_msg: Mapped[str] = mapped_column(TEXT(collation="utf8mb4_general_ci"), default="", server_default=text("''"))
    last_time: Mapped[Optional[datetime]] = mapped_column(DATETIME)
    created_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)
    updated_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)


class ConversationUnread(HeiBase):
    """Per-user unread count per conversation."""
    __tablename__ = "im_conversation_unread"
    __table_args__ = (
        Index("idx_conv_unread", "conversation_id", "user_id", "user_type", unique=True),
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), primary_key=True)
    conversation_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    user_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    user_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    unread_count: Mapped[int] = mapped_column(INTEGER, default=0, server_default=text("0"))


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
