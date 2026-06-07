"""Broadcast (全站通知) ORM models."""

from __future__ import annotations

from datetime import datetime
from typing import Optional
from sqlalchemy import String, Text, DateTime, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column
from core.plugin.registry import HeiBase


class Broadcast(HeiBase):
    """全站通知/公告"""
    __tablename__ = "im_broadcast"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    content: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    scope: Mapped[str] = mapped_column(String(20), nullable=False, default="ALL")  # ALL | BUSINESS | CONSUMER
    sender_id: Mapped[str] = mapped_column(String(32), nullable=False)
    created_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    updated_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)


class BroadcastRead(HeiBase):
    """广播已读记录"""
    __tablename__ = "im_broadcast_read"

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    broadcast_id: Mapped[str] = mapped_column(String(32), nullable=False)
    user_id: Mapped[str] = mapped_column(String(32), nullable=False)
    user_type: Mapped[str] = mapped_column(String(20), nullable=False)
    read_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)

    __table_args__ = (
        UniqueConstraint("broadcast_id", "user_id", "user_type", name="uq_br_broadcast_user"),
    )
