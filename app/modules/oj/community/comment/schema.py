from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.oj.enums import (
    OjCommentTargetType,
    OjContentStatus,
)


class OjCommentAdminPageQuery(ApiSchema):
    pagination: PageQuery
    problem_id: str | None = Field(default=None, max_length=64)
    contest_id: str | None = Field(default=None, max_length=64)
    submission_id: str | None = Field(default=None, max_length=64)
    participation_id: str | None = Field(default=None, max_length=64)
    account_type: str | None = Field(default=None, max_length=32)
    account_id: str | None = Field(default=None, max_length=64)
    target_type: str | None = Field(default=None, max_length=32)
    target_id: str | None = Field(default=None, max_length=64)
    code: str | None = Field(default=None, max_length=64)
    key: str | None = Field(default=None, max_length=64)
    name: str | None = Field(default=None, max_length=255)
    title: str | None = Field(default=None, max_length=255)
    status: str | None = Field(default=None, max_length=32)


class OjCommentTimestampSchema(ApiSchema):
    created_at: datetime | None = None
    created_by: str | None = None
    updated_at: datetime | None = None
    updated_by: str | None = None


class OjCommentCreateRequest(ApiSchema):
    target_type: OjCommentTargetType
    target_id: str = Field(min_length=1, max_length=64)
    parent_id: str | None = Field(default=None, max_length=64)
    account_type: str = Field(min_length=1, max_length=32)
    account_id: str = Field(min_length=1, max_length=64)
    content: str = Field(min_length=1)
    status: OjContentStatus = OjContentStatus.PUBLISHED
    score: int = 0
    reply_count: int = 0
    extra: dict[str, Any] = Field(default_factory=dict)


class OjCommentUpdateRequest(OjCommentCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class OjCommentSchema(OjCommentCreateRequest, OjCommentTimestampSchema):
    id: str
