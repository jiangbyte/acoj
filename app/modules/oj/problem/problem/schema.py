from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.config.enums import StatusEnum
from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.oj.enums import (
    OjJudgeMode,
    OjProblemType,
    OjProblemVisibility,
)


class OjProblemAdminPageQuery(ApiSchema):
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


class OjProblemTimestampSchema(ApiSchema):
    created_at: datetime | None = None
    created_by: str | None = None
    updated_at: datetime | None = None
    updated_by: str | None = None


class OjProblemCreateRequest(ApiSchema):
    code: str = Field(min_length=1, max_length=64)
    title: str = Field(min_length=1, max_length=255)
    summary: str | None = Field(default=None, max_length=500)
    description: str | None = None
    input_description: str | None = None
    output_description: str | None = None
    source: str | None = Field(default=None, max_length=255)
    difficulty: int = 0
    problem_type: OjProblemType = OjProblemType.PROGRAM
    judge_mode: OjJudgeMode = OjJudgeMode.STANDARD
    visibility: OjProblemVisibility = OjProblemVisibility.PUBLIC
    time_limit_ms: int = 1000
    memory_limit_kb: int = 262144
    stack_limit_kb: int | None = None
    output_limit_kb: int | None = None
    points: float = 100.0
    partial: bool = False
    allow_languages: list[str] = Field(default_factory=list)
    spj_language_id: str | None = Field(default=None, max_length=64)
    spj_source: str | None = None
    interactor_language_id: str | None = Field(default=None, max_length=64)
    interactor_source: str | None = None
    remote_provider: str | None = Field(default=None, max_length=64)
    remote_problem_id: str | None = Field(default=None, max_length=128)
    accepted_count: int = 0
    submit_count: int = 0
    ac_rate: float = 0.0
    sort: int = 0
    status: StatusEnum = StatusEnum.ENABLED
    extra: dict[str, Any] = Field(default_factory=dict)


class OjProblemUpdateRequest(OjProblemCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class OjProblemSchema(OjProblemCreateRequest, OjProblemTimestampSchema):
    id: str
