"""Broadcast (全站通知) ORM models."""

from __future__ import annotations

from datetime import datetime
from typing import Optional
from sqlalchemy import Index, text
from sqlalchemy.dialects.mysql import DATETIME, TEXT, VARCHAR
from sqlalchemy.orm import Mapped, mapped_column
from sdk.kernel.registry import HeiBase


# ─── Broadcast scope constants ─────────────────────────────────────────

BroadcastScopeAll = "ALL"
BroadcastScopeBusiness = "BUSINESS"
BroadcastScopeConsumer = "CONSUMER"


class Broadcast(HeiBase):
    """全站通知/公告"""
    __tablename__ = "im_broadcast"

    id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), primary_key=True)
    title: Mapped[str] = mapped_column(VARCHAR(255, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    content: Mapped[str] = mapped_column(TEXT(collation="utf8mb4_general_ci"))
    scope: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False, default="ALL", server_default=text("'ALL'"))
    sender_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    created_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)
    updated_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)


class BroadcastRead(HeiBase):
    """广播已读记录"""
    __tablename__ = "im_broadcast_read"
    __table_args__ = (
        Index("idx_br_broadcast_user", "broadcast_id", "user_id", "user_type", unique=True),
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), primary_key=True)
    broadcast_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    user_id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    user_type: Mapped[str] = mapped_column(VARCHAR(20, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    read_at: Mapped[Optional[datetime]] = mapped_column(DATETIME)
