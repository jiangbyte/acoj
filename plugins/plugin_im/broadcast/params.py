"""Broadcast module parameter/result models."""

from __future__ import annotations

from pydantic import BaseModel, Field
from typing import Optional


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
