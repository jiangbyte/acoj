from datetime import datetime

from pydantic import Field

from app.core.config.enums import AccountType
from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.message.enums import (
    MessageContentType,
    MessageTargetScope,
    TodoAssigneeStatus,
    TodoPriority,
    TodoStatus,
)
from app.modules.message.schema import AccountRef


class TodoCreateRequest(ApiSchema):
    title: str = Field(min_length=1, max_length=255)
    content: str | None = None
    content_type: MessageContentType = MessageContentType.TEXT
    priority: TodoPriority = TodoPriority.NORMAL
    target_scope: MessageTargetScope = MessageTargetScope.SPECIFIC
    target_account_type: AccountType | None = None
    target_account_id: str | None = Field(default=None, max_length=64)
    assignee_refs: list[AccountRef] = Field(default_factory=list)
    source_type: str | None = Field(default=None, max_length=64)
    source_id: str | None = Field(default=None, max_length=64)
    status: TodoStatus = TodoStatus.PENDING
    due_at: datetime | None = None
    extra: dict = Field(default_factory=dict)


class TodoUpdateRequest(TodoCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class TodoAdminPageQuery(ApiSchema):
    pagination: PageQuery
    title: str | None = Field(default=None, max_length=255)
    status: TodoStatus | None = None
    target_account_type: AccountType | None = None


class MyTodoPageQuery(ApiSchema):
    pagination: PageQuery
    include_done: bool = False


class TodoSchema(ApiSchema):
    id: str
    title: str
    content: str | None = None
    content_type: MessageContentType | str
    priority: TodoPriority | str
    target_scope: MessageTargetScope | str
    target_account_type: AccountType | str | None = None
    target_account_id: str | None = None
    creator_account_type: AccountType | str | None = None
    creator_account_id: str | None = None
    source_type: str | None = None
    source_id: str | None = None
    status: TodoStatus | str
    assignee_status: TodoAssigneeStatus | str | None = None
    due_at: datetime | None = None
    extra: dict
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class TodoStatusRequest(ApiSchema):
    todo_id: str = Field(min_length=1, max_length=64)
