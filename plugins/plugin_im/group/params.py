"""Group module parameter/result models."""

from __future__ import annotations

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field

from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin


class CreateParam(BaseModel):
    name: str = Field(..., description="群名称")
    avatar: str = Field("", description="群头像")
    member_ids: list[str] = Field(default_factory=list, description="初始成员ID列表")
    member_type: str = Field("", description="成员类型: BUSINESS | CONSUMER")


class UpdateParam(BaseModel):
    group_id: str = Field(..., description="群ID")
    name: Optional[str] = Field(None, description="群名称")
    avatar: Optional[str] = Field(None, description="群头像")
    notice: Optional[str] = Field(None, description="群公告")


class GroupIdParam(BaseModel):
    group_id: str = Field(..., description="群ID")


class InviteParam(BaseModel):
    group_id: str = Field(..., description="群ID")
    user_ids: list[str] = Field(..., description="用户ID列表")
    user_type: str = Field(..., description="用户类型")


class KickParam(BaseModel):
    group_id: str = Field(..., description="群ID")
    user_id: str = Field(..., description="用户ID")
    user_type: str = Field(..., description="用户类型")


class SetRoleParam(BaseModel):
    group_id: str = Field(..., description="群ID")
    user_id: str = Field(..., description="用户ID")
    user_type: str = Field(..., description="用户类型")
    role: str = Field(..., description="角色: admin | owner")


class SendMessageParam(BaseModel):
    group_id: str = Field(..., description="群ID")
    content: str = Field("", description="消息内容")
    msg_type: str = Field("TEXT", description="消息类型")
    extra: str = Field("", description="扩展JSON")
    reply_to: str = Field("", description="回复消息ID")


class RecallMessageParam(BaseModel):
    group_id: str = Field(..., description="群ID")
    message_id: str = Field(..., description="消息ID")


class MarkReadParam(BaseModel):
    group_id: str = Field(..., description="群ID")
    message_id: str = Field("", description="消息ID")


class MuteMemberParam(BaseModel):
    group_id: str = Field(..., description="群ID")
    user_id: str = Field(..., description="用户ID")
    user_type: str = Field(..., description="用户类型")
    duration: int = Field(60, description="禁言分钟数")


class HandleJoinRequestParam(BaseModel):
    request_id: str = Field(..., description="请求ID")
    action: str = Field(..., description="操作: approved | rejected")


class TransferOwnerParam(BaseModel):
    group_id: str = Field(..., description="群ID")
    new_owner_id: str = Field(..., description="新群主ID")
    new_owner_type: str = Field(..., description="新群主类型")


class SetNicknameParam(BaseModel):
    group_id: str = Field(..., description="群ID")
    user_id: str = Field(..., description="用户ID")
    user_type: str = Field(..., description="用户类型")
    nickname: str = Field("", description="群昵称")


class GroupVO(BaseModel):
    id: str = ""
    name: str = ""
    avatar: str = ""
    owner_id: str = ""
    owner_type: str = ""
    group_type: str = ""
    notice: str = ""
    member_count: int = 0
    last_content: str = ""
    last_time: str = ""
    unread_count: int = 0


class MemberVO(BaseModel):
    user_id: str = ""
    user_type: str = ""
    role: str = ""
    nickname: str = ""
    joined_at: str = ""
    is_muted: bool = False


class MessageVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: str = ""
    sender_id: str = ""
    sender_type: str = ""
    content: str = ""
    extra: str = ""
    msg_type: str = ""
    reply_to: str = ""
    file_url: str = ""
    created_at: Optional[datetime] = None


class ConversationVO(BaseModel):
    conversation_id: str = ""
    conversation_type: str = ""
    other_user_id: str = ""
    other_user_type: str = ""
    other_nickname: str = ""
    other_avatar: str = ""
    group_id: str = ""
    group_name: str = ""
    group_avatar: str = ""
    member_count: int = 0
    last_content: str = ""
    last_time: str = ""
    unread_count: int = 0


class JoinRequestVO(BaseModel):
    id: str = ""
    group_id: str = ""
    user_id: str = ""
    user_type: str = ""
    remark: str = ""
    created_at: str = ""


class GroupSearchVO(BaseModel):
    id: str = ""
    name: str = ""
    avatar: str = ""
    member_count: int = 0
