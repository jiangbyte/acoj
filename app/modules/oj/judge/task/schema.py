from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.oj.enums import (
    OjJudgeTaskStatus,
    OjJudgeTaskType,
)


class OjJudgeTaskAdminPageQuery(ApiSchema):
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


class OjJudgeTaskTimestampSchema(ApiSchema):
    created_at: datetime | None = None
    created_by: str | None = None
    updated_at: datetime | None = None
    updated_by: str | None = None


class OjJudgeTaskCreateRequest(ApiSchema):
    submission_id: str = Field(min_length=1, max_length=64)
    problem_id: str = Field(min_length=1, max_length=64)
    judge_node_id: str | None = Field(default=None, max_length=64)
    task_type: OjJudgeTaskType = OjJudgeTaskType.JUDGE
    priority: int = 0
    status: OjJudgeTaskStatus = OjJudgeTaskStatus.PENDING
    attempts: int = 0
    locked_at: datetime | None = None
    started_at: datetime | None = None
    finished_at: datetime | None = None
    error: str | None = None
    payload: dict[str, Any] = Field(default_factory=dict)
    result_payload: dict[str, Any] = Field(default_factory=dict)


class OjJudgeTaskUpdateRequest(OjJudgeTaskCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class OjJudgeTaskSchema(OjJudgeTaskCreateRequest, OjJudgeTaskTimestampSchema):
    id: str
