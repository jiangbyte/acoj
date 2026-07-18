from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.oj.enums import (
    OjJudgeMode,
    OjJudgeResult,
    OjSubmitStatus,
)


class OjSubmissionAdminPageQuery(ApiSchema):
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


class OjSubmissionTimestampSchema(ApiSchema):
    created_at: datetime | None = None
    created_by: str | None = None
    updated_at: datetime | None = None
    updated_by: str | None = None


class OjSubmissionCreateRequest(ApiSchema):
    problem_id: str = Field(min_length=1, max_length=64)
    problem_code: str = Field(min_length=1, max_length=64)
    account_type: str = Field(min_length=1, max_length=32)
    account_id: str = Field(min_length=1, max_length=64)
    language_id: str | None = Field(default=None, max_length=64)
    judge_mode: OjJudgeMode = OjJudgeMode.STANDARD
    status: OjSubmitStatus = OjSubmitStatus.QUEUED
    result: OjJudgeResult | None = None
    score: float | None = None
    time_ms: int | None = None
    memory_kb: int | None = None
    current_case: int = 0
    case_points: float = 0.0
    case_total: float = 0.0
    compile_output: str | None = None
    judge_node_id: str | None = Field(default=None, max_length=64)
    submitted_at: datetime | None = None
    judged_at: datetime | None = None
    rejudged_at: datetime | None = None
    contest_id: str | None = Field(default=None, max_length=64)
    contest_problem_id: str | None = Field(default=None, max_length=64)
    participation_id: str | None = Field(default=None, max_length=64)
    is_pretest: bool = False
    is_archived: bool = False
    source_visibility: str | None = Field(default=None, max_length=32)
    extra: dict[str, Any] = Field(default_factory=dict)


class OjSubmissionUpdateRequest(OjSubmissionCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class OjSubmissionSchema(OjSubmissionCreateRequest, OjSubmissionTimestampSchema):
    id: str
