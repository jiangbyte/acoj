from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.oj.enums import (
    OjTestCaseType,
)


class OjTestCaseAdminPageQuery(ApiSchema):
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


class OjTestCaseTimestampSchema(ApiSchema):
    created_at: datetime | None = None
    created_by: str | None = None
    updated_at: datetime | None = None
    updated_by: str | None = None


class OjTestCaseCreateRequest(ApiSchema):
    dataset_id: str = Field(min_length=1, max_length=64)
    case_no: int
    case_type: OjTestCaseType = OjTestCaseType.NORMAL
    input_file: str | None = Field(default=None, max_length=255)
    output_file: str | None = Field(default=None, max_length=255)
    input_inline: str | None = None
    output_inline: str | None = None
    generator_args: str | None = None
    points: float | None = None
    is_pretest: bool = False
    batch_no: int | None = None
    batch_dependencies: list[int] = Field(default_factory=list)
    time_limit_ms: int | None = None
    memory_limit_kb: int | None = None
    checker: str | None = Field(default=None, max_length=64)
    checker_args: dict[str, Any] = Field(default_factory=dict)
    sort: int = 0


class OjTestCaseUpdateRequest(OjTestCaseCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class OjTestCaseSchema(OjTestCaseCreateRequest, OjTestCaseTimestampSchema):
    id: str
