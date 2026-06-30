from datetime import datetime

from pydantic import Field, model_validator

from app.core.config.enums import AccountType
from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.message.enums import (
    MessageContentType,
    MessageGroupStatus,
    MessageTargetScope,
    MessageThreadStatus,
    MessageThreadType,
    NotificationSeverity,
    NotificationStatus,
    TodoAssigneeStatus,
    TodoPriority,
    TodoStatus,
)


class NotificationCreateRequest(ApiSchema):
    title: str = Field(min_length=1, max_length=255)
    content: str = Field(min_length=1)
    content_type: MessageContentType = MessageContentType.TEXT
    severity: NotificationSeverity = NotificationSeverity.INFO
    target_scope: MessageTargetScope = MessageTargetScope.ALL
    target_account_type: AccountType | None = None
    target_account_id: str | None = Field(default=None, max_length=64)
    status: NotificationStatus = NotificationStatus.DRAFT
    publish_at: datetime | None = None
    extra: dict = Field(default_factory=dict)

class NotificationUpdateRequest(NotificationCreateRequest):
    id: str = Field(min_length=1, max_length=64)

class NotificationAdminPageQuery(ApiSchema):
    pagination: PageQuery
    title: str | None = Field(default=None, max_length=255)
    status: NotificationStatus | None = None
    target_account_type: AccountType | None = None

class MyNotificationPageQuery(ApiSchema):
    pagination: PageQuery
    unread_only: bool = False

class NotificationSchema(ApiSchema):
    id: str
    title: str
    content: str
    content_type: MessageContentType | str
    severity: NotificationSeverity | str
    target_scope: MessageTargetScope | str
    target_account_type: AccountType | str | None = None
    target_account_id: str | None = None
    sender_account_type: AccountType | str | None = None
    sender_account_id: str | None = None
    status: NotificationStatus | str
    publish_at: datetime | None = None
    revoked_at: datetime | None = None
    is_read: bool = False
    extra: dict
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None

class MarkNotificationReadRequest(ApiSchema):
    ids: list[str] = Field(min_length=1)
