from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.oj.enums import (
    OjObjectiveAnswerType,
)


class OjObjectiveAnswerAdminPageQuery(ApiSchema):
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


class OjObjectiveAnswerTimestampSchema(ApiSchema):
    created_at: datetime | None = None
    created_by: str | None = None
    updated_at: datetime | None = None
    updated_by: str | None = None


class OjObjectiveAnswerCreateRequest(ApiSchema):
    problem_id: str = Field(min_length=1, max_length=64)
    answer_type: OjObjectiveAnswerType = OjObjectiveAnswerType.SINGLE
    answer: dict[str, Any] = Field(default_factory=dict)
    score_rule: dict[str, Any] = Field(default_factory=dict)
    explanation: str | None = None
    extra: dict[str, Any] = Field(default_factory=dict)


class OjObjectiveAnswerUpdateRequest(OjObjectiveAnswerCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class OjObjectiveAnswerSchema(OjObjectiveAnswerCreateRequest, OjObjectiveAnswerTimestampSchema):
    id: str
