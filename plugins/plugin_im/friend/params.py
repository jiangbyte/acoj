"""Friend module parameter/result models."""

from __future__ import annotations

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field

from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin


class SendRequestParam(BaseModel):
    receiver_id: str = Field(..., description="接收者ID")
    receiver_type: str = Field(..., description="接收者类型: BUSINESS | CONSUMER")
    remark: str = Field("", description="备注")


class HandleRequestParam(BaseModel):
    request_id: str = Field(..., description="请求ID")


class RemoveFriendParam(BaseModel):
    friend_id: str = Field(..., description="好友ID")
    friend_type: str = Field(..., description="好友类型")


class FriendVO(BaseModel):
    user_id: str = ""
    user_type: str = ""
    nickname: str = ""
    avatar: str = ""
    remark: str = ""
    added_at: str = ""


class FriendRequestVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: str = ""
    sender_id: str = ""
    sender_type: str = ""
    receiver_id: str = ""
    receiver_type: str = ""
    remark: str = ""
    status: str = ""
    created_at: Optional[datetime] = None


class BlockVO(BaseModel):
    blocked_id: str = ""
    blocked_type: str = ""
    created_at: str = ""


class RemarkParam(BaseModel):
    friend_id: str = Field(..., description="好友ID")
    friend_type: str = Field(..., description="好友类型")
    remark: str = Field("", description="备注")


class BlockParam(BaseModel):
    blocked_id: str = Field(..., description="拉黑用户ID")
    blocked_type: str = Field(..., description="拉黑用户类型")


class SearchResult(BaseModel):
    user_id: str = ""
    user_type: str = ""
    nickname: str = ""
    avatar: str = ""


class PendingRequestsResult(BaseModel):
    incoming: list[FriendRequestVO] = Field(default_factory=list)
    outgoing: list[FriendRequestVO] = Field(default_factory=list)
