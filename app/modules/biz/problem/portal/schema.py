"""Portal problem bank schemas."""

from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.biz.problem.enums import SubmissionSourceVisibility


class PortalProblemPageQuery(ApiSchema):
    pagination: PageQuery
    keyword: str | None = None
    code: str | None = None
    name: str | None = None
    group_id: str | None = None
    type_id: str | None = None


class PortalProblemListSchema(ApiSchema):
    id: str
    code: str
    name: str
    summary: str | None = None
    group_id: str | None = None
    group_name: str | None = None
    time_limit_ms: int
    memory_limit_kb: int
    points: float
    partial: bool
    user_count: int
    ac_rate: float
    type_ids: list[str] = Field(default_factory=list)
    type_names: list[str] = Field(default_factory=list)


class PortalProblemDetailSchema(PortalProblemListSchema):
    description: str
    submission_source_visibility: SubmissionSourceVisibility
    published_at: datetime | None = None
    extra: dict[str, Any] | None = Field(default_factory=dict)


class PortalProblemLanguageSchema(ApiSchema):
    language_key: str
    label: str | None = None
    extension: str | None = None
    time_limit_ms: int | None = None
    memory_limit_kb: int | None = None


class PortalProblemSubmitRequest(ApiSchema):
    language_key: str = Field(min_length=1, max_length=32)
    source: str = Field(min_length=1)
    wait: bool = False
    wait_timeout_sec: int = Field(default=60, ge=5, le=300)
