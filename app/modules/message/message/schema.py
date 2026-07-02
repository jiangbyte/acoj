from datetime import datetime

from pydantic import Field

from app.core.config.enums import AccountType
from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.message.enums import (
    MessageContentType,
    MessageGroupStatus,
    MessageThreadStatus,
    MessageThreadType,
)
from app.modules.message.schema import AccountRef as AccountRef


class GroupCreateRequest(ApiSchema):
    name: str = Field(min_length=1, max_length=128)
    avatar: str | None = Field(default=None, max_length=500)
    description: str | None = None
    member_refs: list[AccountRef] = Field(default_factory=list)
    extra: dict = Field(default_factory=dict)


class GroupUpdateRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    name: str = Field(min_length=1, max_length=128)
    avatar: str | None = Field(default=None, max_length=500)
    status: MessageGroupStatus = MessageGroupStatus.ENABLED
    description: str | None = None
    extra: dict = Field(default_factory=dict)


class GroupMemberRequest(ApiSchema):
    group_id: str = Field(min_length=1, max_length=64)
    member_refs: list[AccountRef] = Field(min_length=1)


class GroupPageQuery(ApiSchema):
    pagination: PageQuery
    name: str | None = Field(default=None, max_length=128)
    status: MessageGroupStatus | None = None


class GroupSchema(ApiSchema):
    id: str
    name: str
    owner_account_type: AccountType | str | None = None
    owner_account_id: str | None = None
    avatar: str | None = None
    status: MessageGroupStatus | str
    description: str | None = None
    member_count: int = 0
    extra: dict
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class GroupMemberSchema(ApiSchema):
    id: str
    group_id: str
    account_type: AccountType | str
    account_id: str
    nickname: str | None = None
    is_muted: bool
    joined_at: datetime
    left_at: datetime | None = None


class ThreadPageQuery(ApiSchema):
    pagination: PageQuery
    thread_type: MessageThreadType | None = None
    status: MessageThreadStatus | None = None


class ThreadCreateRequest(ApiSchema):
    thread_type: MessageThreadType = MessageThreadType.DIRECT
    title: str | None = Field(default=None, max_length=255)
    group_id: str | None = Field(default=None, max_length=64)
    participant_refs: list[AccountRef] = Field(default_factory=list)
    extra: dict = Field(default_factory=dict)


class SendMessageRequest(ApiSchema):
    thread_id: str | None = Field(default=None, max_length=64)
    group_id: str | None = Field(default=None, max_length=64)
    participant_refs: list[AccountRef] = Field(default_factory=list)
    title: str | None = Field(default=None, max_length=255)
    parent_id: str | None = Field(default=None, max_length=64)
    content: str = Field(min_length=1)
    content_type: MessageContentType = MessageContentType.TEXT
    sender_name: str | None = Field(default=None, max_length=128)
    attachments: list["MessageAttachmentInput"] = Field(default_factory=list)
    extra: dict = Field(default_factory=dict)


class MessageAttachmentInput(ApiSchema):
    name: str = Field(min_length=1, max_length=255)
    url: str = Field(min_length=1, max_length=1024)
    content_type: str | None = Field(default=None, max_length=128)
    size: int | None = Field(default=None, ge=0)
    sort: int = 0
    extra: dict = Field(default_factory=dict)


class ThreadMessagePageQuery(ApiSchema):
    pagination: PageQuery
    thread_id: str


class MessageAttachmentSchema(ApiSchema):
    id: str
    message_id: str
    name: str
    url: str
    content_type: str | None = None
    size: int | None = None
    sort: int
    extra: dict


class MessageReactionSummary(ApiSchema):
    reaction: str
    count: int
    reacted: bool = False


class MessageSchema(ApiSchema):
    id: str
    thread_id: str
    parent_id: str | None = None
    sender_type: str
    sender_account_type: AccountType | str | None = None
    sender_account_id: str | None = None
    sender_name: str | None = None
    content: str
    content_type: MessageContentType | str
    reply_count: int
    is_revoked: bool
    attachments: list[MessageAttachmentSchema] = Field(default_factory=list)
    reactions: list[MessageReactionSummary] = Field(default_factory=list)
    extra: dict
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class ThreadSchema(ApiSchema):
    id: str
    thread_type: MessageThreadType | str
    title: str | None = None
    group_id: str | None = None
    created_account_type: AccountType | str | None = None
    created_account_id: str | None = None
    status: MessageThreadStatus | str
    last_message_id: str | None = None
    last_message_at: datetime | None = None
    unread_count: int = 0
    last_message: MessageSchema | None = None
    extra: dict
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class ReadThreadRequest(ApiSchema):
    thread_id: str = Field(min_length=1, max_length=64)


class ReactMessageRequest(ApiSchema):
    message_id: str = Field(min_length=1, max_length=64)
    reaction: str = Field(min_length=1, max_length=64)
