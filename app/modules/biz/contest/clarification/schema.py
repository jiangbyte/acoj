"""Contest clarification admin schemas."""

from datetime import datetime

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.biz.contest.enums import ClarificationThreadStatus


class OjContestClarificationCreateRequest(ApiSchema):
    problem_id: str | None = None
    title: str = Field(min_length=1, max_length=200)
    body: str = Field(min_length=1)
    published_at: datetime | None = None


class OjContestClarificationUpdateRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    problem_id: str | None = None
    title: str = Field(min_length=1, max_length=200)
    body: str = Field(min_length=1)
    published_at: datetime | None = None


class OjContestClarificationAdminPageQuery(ApiSchema):
    pagination: PageQuery
    contest_id: str | None = None
    problem_id: str | None = None


class OjContestClarificationSchema(ApiSchema):
    id: str
    contest_id: str
    problem_id: str | None = None
    title: str
    body: str
    published_at: datetime
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class OjContestClarificationThreadAdminPageQuery(ApiSchema):
    pagination: PageQuery
    contest_id: str | None = None
    status: ClarificationThreadStatus | None = None
    account_id: str | None = None


class OjContestClarificationMessageSchema(ApiSchema):
    id: str
    thread_id: str
    account_id: str
    body: str
    is_staff: bool
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class OjContestClarificationThreadSchema(ApiSchema):
    id: str
    contest_id: str
    problem_id: str | None = None
    account_id: str
    title: str
    status: str
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None
    messages: list[OjContestClarificationMessageSchema] = Field(default_factory=list)


class OjContestClarificationThreadReplyRequest(ApiSchema):
    thread_id: str = Field(min_length=1, max_length=64)
    body: str = Field(min_length=1)
    set_answered: bool = True


class OjContestClarificationThreadStatusRequest(ApiSchema):
    thread_id: str = Field(min_length=1, max_length=64)
    status: ClarificationThreadStatus


class OjContestClarificationThreadPromoteRequest(ApiSchema):
    thread_id: str = Field(min_length=1, max_length=64)
    title: str | None = Field(default=None, max_length=200)
    body: str | None = None
