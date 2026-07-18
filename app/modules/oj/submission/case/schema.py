from datetime import datetime

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.oj.enums import (
    OjJudgeResult,
    OjSubmitStatus,
)


class OjSubmissionCaseAdminPageQuery(ApiSchema):
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


class OjSubmissionCaseTimestampSchema(ApiSchema):
    created_at: datetime | None = None
    created_by: str | None = None
    updated_at: datetime | None = None
    updated_by: str | None = None


class OjSubmissionCaseCreateRequest(ApiSchema):
    submission_id: str = Field(min_length=1, max_length=64)
    case_no: int
    status: OjSubmitStatus = OjSubmitStatus.QUEUED
    result: OjJudgeResult | None = None
    time_ms: int | None = None
    memory_kb: int | None = None
    points: float | None = None
    total: float | None = None
    batch_no: int | None = None
    feedback: str | None = Field(default=None, max_length=255)
    extended_feedback: str | None = None
    output: str | None = None
    stderr: str | None = None
    sort: int = 0


class OjSubmissionCaseUpdateRequest(OjSubmissionCaseCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class OjSubmissionCaseSchema(OjSubmissionCaseCreateRequest, OjSubmissionCaseTimestampSchema):
    id: str
