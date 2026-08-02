"""Portal contest request/response schemas."""

from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.biz.contest.enums import ClarificationThreadStatus


class PortalContestJoinRequest(ApiSchema):
    access_code: str | None = None
    spectate: bool = False


class PortalContestSubmitRequest(ApiSchema):
    problem_id: str
    language_key: str
    source: str
    wait: bool = False
    wait_timeout_sec: int = 60


class PortalClarificationThreadCreateRequest(ApiSchema):
    title: str
    body: str
    problem_id: str | None = None


class PortalClarificationMessageCreateRequest(ApiSchema):
    body: str


class PortalContestParticipationSchema(ApiSchema):
    id: str
    contest_id: str
    account_id: str
    real_start: datetime
    score: float
    cumtime: int
    virtual: int
    is_disqualified: bool


class PortalClarificationSchema(ApiSchema):
    id: str
    contest_id: str
    problem_id: str | None = None
    title: str
    body: str
    published_at: datetime


class PortalClarificationMessageSchema(ApiSchema):
    id: str
    thread_id: str
    account_id: str
    body: str
    is_staff: bool
    created_at: datetime


class PortalClarificationThreadSchema(ApiSchema):
    id: str
    contest_id: str
    problem_id: str | None = None
    account_id: str
    title: str
    status: ClarificationThreadStatus
    messages: list[PortalClarificationMessageSchema] = Field(default_factory=list)


class PortalContestBriefSchema(ApiSchema):
    id: str
    key: str
    name: str
    summary: str | None = None
    description: str | None = None
    start_time: datetime
    end_time: datetime
    format_name: str
    lifecycle_status: str
    is_rated: bool
    is_private: bool = False
    use_clarifications: bool
    scoreboard_visibility: str
    freeze_seconds: int | None = None
    user_count: int = 0
    joined: bool = False
    extra: dict[str, Any] | None = Field(default_factory=dict)


class PortalContestPageQuery(ApiSchema):
    pagination: PageQuery
    keyword: str | None = None


class PortalContestProblemMetaSchema(ApiSchema):
    id: str
    problem_id: str
    label: str
    points: float
    partial: bool
    sort: int
    max_submissions: int | None = None
    problem_code: str | None = None
    problem_name: str | None = None


class PortalContestProblemDetailSchema(ApiSchema):
    id: str
    problem_id: str
    label: str
    points: float
    partial: bool
    sort: int
    max_submissions: int | None = None
    problem_code: str
    problem_name: str
    description: str
    time_limit_ms: int
    memory_limit_kb: int
    languages: list[dict[str, Any]] = Field(default_factory=list)
