from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.oj.enums import (
    OjJudgeResult,
    OjJudgeTaskStatus,
)


class OjRejudgeRecordAdminPageQuery(ApiSchema):
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


class OjRejudgeRecordTimestampSchema(ApiSchema):
    created_at: datetime | None = None
    created_by: str | None = None
    updated_at: datetime | None = None
    updated_by: str | None = None


class OjRejudgeRecordCreateRequest(ApiSchema):
    submission_id: str = Field(min_length=1, max_length=64)
    operator_account_type: str | None = Field(default=None, max_length=32)
    operator_account_id: str | None = Field(default=None, max_length=64)
    reason: str | None = None
    old_result: OjJudgeResult | None = None
    new_result: OjJudgeResult | None = None
    old_score: float | None = None
    new_score: float | None = None
    started_at: datetime | None = None
    finished_at: datetime | None = None
    status: OjJudgeTaskStatus = OjJudgeTaskStatus.PENDING
    extra: dict[str, Any] = Field(default_factory=dict)


class OjRejudgeRecordUpdateRequest(OjRejudgeRecordCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class OjRejudgeRecordSchema(OjRejudgeRecordCreateRequest, OjRejudgeRecordTimestampSchema):
    id: str
