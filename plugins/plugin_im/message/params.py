"""Message module parameter/result models."""

from __future__ import annotations

from pydantic import BaseModel, Field
from typing import Optional
from plugins.plugin_im.model.message import Message
from plugins.plugin_im.model.im_file import ImFile


class MessageVO(BaseModel):
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
    read_at: Optional[str] = None
    created_at: str = ""
    updated_at: str = ""


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


class GetOrCreateConversationParam(BaseModel):
    user_id: str = Field(..., description="对方用户ID")
    user_type: str = Field(..., description="对方用户类型")


class UploadFileResult(BaseModel):
    url: str = ""
    file_key: str = ""
    bucket: str = ""
    engine: str = ""
    original_name: str = ""
    file_size: int = 0
    file_type: str = ""


class ImFileVO(BaseModel):
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
    created_at: str = ""


# Conversation type constants
ConvTypeSingle = "single"
ConvTypeGroup = "group"


def MessageToMessageVO(src: Optional[Message]) -> Optional[MessageVO]:
    if src is None:
        return None
    return MessageVO(
        id=src.id,
        conversation_id=src.conversation_id,
        content=src.content or "",
        msg_type=src.msg_type,
        extra=src.extra or "",
        sender_id=src.sender_id or "",
        sender_type=src.sender_type or "",
        receiver_id=src.receiver_id or "",
        receiver_type=src.receiver_type or "",
        status=src.status,
        read_at=src.read_at.strftime("%Y-%m-%d %H:%M:%S") if src.read_at else None,
        created_at=src.created_at.strftime("%Y-%m-%d %H:%M:%S") if src.created_at else "",
        updated_at=src.updated_at.strftime("%Y-%m-%d %H:%M:%S") if src.updated_at else "",
    )


def ImFileToImFileVO(src: Optional[ImFile]) -> Optional[ImFileVO]:
    if src is None:
        return None
    return ImFileVO(
        id=src.id or "",
        engine=src.engine or "",
        bucket=src.bucket or "",
        file_key=src.file_key or "",
        name=src.name or "",
        suffix=src.suffix or "",
        size_kb=src.size_kb or 0,
        size_info=src.size_info or "",
        download_path=src.download_path or "",
        thumbnail=src.thumbnail or "",
        conversation_id=src.conversation_id or "",
        sender_id=src.sender_id or "",
        sender_type=src.sender_type or "",
        msg_type=src.msg_type or "",
        created_at=src.created_at.strftime("%Y-%m-%d %H:%M:%S") if src.created_at else "",
    )
