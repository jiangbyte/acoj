"""Broadcast module parameter/result models."""

from __future__ import annotations

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field

from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin


class SendBroadcastParam(BaseModel):
    title: str = Field(..., description="通知标题")
    content: str = Field("", description="通知内容")
    scope: str = Field("ALL", description="范围: ALL | BUSINESS | CONSUMER")


class BroadcastReadParam(BaseModel):
    broadcast_id: str = Field(..., description="通知ID")


class BroadcastVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: str = ""
    title: str = ""
    content: str = ""
    scope: str = ""
    sender_id: str = ""
    read: bool = False
    read_at: str = ""
    created_at: Optional[datetime] = None
