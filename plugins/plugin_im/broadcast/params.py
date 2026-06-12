"""Broadcast module parameter/result models."""

from __future__ import annotations

from pydantic import BaseModel, Field
from typing import Optional
from .models import Broadcast


class SendBroadcastParam(BaseModel):
    title: str = Field(..., description="通知标题")
    content: str = Field("", description="通知内容")
    scope: str = Field("ALL", description="范围: ALL | BUSINESS | CONSUMER")


class BroadcastVO(BaseModel):
    id: str = ""
    title: str = ""
    content: str = ""
    scope: str = ""
    sender_id: str = ""
    read: bool = False
    read_at: str = ""
    created_at: str = ""


def BroadcastToBroadcastVO(src: Optional[Broadcast]) -> Optional[BroadcastVO]:
    if src is None:
        return None
    return BroadcastVO(
        id=src.id,
        title=src.title,
        content=src.content or "",
        scope=src.scope,
        sender_id=src.sender_id,
        created_at=src.created_at.strftime("%Y-%m-%d %H:%M:%S") if src.created_at else "",
    )
