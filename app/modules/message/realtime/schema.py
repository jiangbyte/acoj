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


class MessageSummary(ApiSchema):
    notification_unread: int = 0
    message_unread: int = 0
    todo_pending: int = 0

class MessageSummaryResponse(MessageSummary):
    total: int = 0

    @model_validator(mode="after")
    def fill_total(self):
        self.total = self.notification_unread + self.message_unread + self.todo_pending
        return self

class HeaderNoticeItem(ApiSchema):
    id: str
    type: int
    title: str
    icon: str
    tag_title: str | None = None
    tag_type: str | None = None
    description: str | None = None
    date: datetime | None = None
    is_read: bool = False
    source_type: str
    source_id: str
