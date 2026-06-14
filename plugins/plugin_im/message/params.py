"""Message module parameter/result models."""

from __future__ import annotations

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field

from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin

from plugins.plugin_im.model.group import GroupMessage


class MessageVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    conversation_id: str = ""
    id: str = ""
    content: str = ""
    msg_type: str = "TEXT"
    extra: str = ""
    sender_id: str = ""
    sender_type: str = ""
    receiver_id: str = ""
    receiver_type: str = ""
    status: str = ""
    read_at: Optional[datetime] = None
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None


class MessagePageParam(BaseModel):
    current: int = Field(1, ge=1)
    size: int = Field(10, ge=1, le=100)
    status: str = ""


class MessageSendParam(BaseModel):
    content: str = ""
    msg_type: str = "TEXT"
    extra: str = ""
    receiver_ids: list[str] = Field(default_factory=list)
    receiver_type: str = "BUSINESS"


class RecallParam(BaseModel):
    message_id: str = Field(...)


class MessageIdsParam(BaseModel):
    ids: list[str] = Field(default_factory=list)


class MessageReadParam(BaseModel):
    id: str = Field(...)


class ForwardParam(BaseModel):
    message_id: str = Field(...)
    target_ids: list[str] = Field(default_factory=list)
    target_type: str = Field("BUSINESS")


class SearchParam(BaseModel):
    keyword: str = ""
    cursor: str = ""
    size: int = Field(20, ge=1, le=100)


class UnreadCountVO(BaseModel):
    count: int = 0


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


class ConversationMessageVO(BaseModel):
    id: str = ""
    sender_id: str = ""
    sender_type: str = ""
    content: str = ""
    msg_type: str = ""
    extra: str = ""
    status: str = ""
    file_url: str = ""
    created_at: str = ""

    @classmethod
    def from_message(
        cls,
        *,
        id: str,
        sender_id: str,
        sender_type: str,
        content: str,
        msg_type: str,
        extra: str,
        status: str,
        file_url: str,
        created_at: Optional[datetime],
    ) -> "ConversationMessageVO":
        return cls(
            id=id,
            sender_id=sender_id,
            sender_type=sender_type,
            content=content,
            msg_type=msg_type,
            extra=extra,
            status=status,
            file_url=file_url,
            created_at=created_at.strftime("%Y-%m-%d %H:%M:%S") if created_at else "",
        )

    @classmethod
    def from_group_message(cls, src: GroupMessage) -> "ConversationMessageVO":
        return cls.from_message(
            id=src.id,
            sender_id=src.sender_id,
            sender_type=src.sender_type,
            content=src.content or "",
            msg_type=src.msg_type,
            extra=src.extra or "",
            status="",
            file_url="",
            created_at=src.created_at,
        )


class GetOrCreateConversationParam(BaseModel):
    user_id: str = Field(..., description="对方用户ID")
    user_type: str = Field(..., description="对方用户类型")


class ConversationReadParam(BaseModel):
    conversation_id: str = Field(..., description="会话ID")


class UploadFileResult(BaseModel):
    url: str = ""
    file_key: str = ""
    bucket: str = ""
    engine: str = ""
    original_name: str = ""
    file_size: int = 0
    file_type: str = ""


class ImFileVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: str = ""
    engine: str = ""
    bucket: str = ""
    file_key: str = ""
    name: str = ""
    suffix: str = ""
    size_kb: int = 0
    size_info: str = ""
    download_path: str = ""
    thumbnail: str = ""
    conversation_id: str = ""
    sender_id: str = ""
    sender_type: str = ""
    msg_type: str = ""
    created_at: Optional[datetime] = None


# Conversation type constants
ConvTypeSingle = "single"
ConvTypeGroup = "group"
