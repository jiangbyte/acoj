from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.oj.enums import (
    OjParticipationType,
)


class OjContestParticipationAdminPageQuery(ApiSchema):
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


class OjContestParticipationTimestampSchema(ApiSchema):
    created_at: datetime | None = None
    created_by: str | None = None
    updated_at: datetime | None = None
    updated_by: str | None = None


class OjContestParticipationCreateRequest(ApiSchema):
    contest_id: str = Field(min_length=1, max_length=64)
    account_type: str = Field(min_length=1, max_length=32)
    account_id: str = Field(min_length=1, max_length=64)
    participation_type: OjParticipationType = OjParticipationType.LIVE
    started_at: datetime | None = None
    ended_at: datetime | None = None
    score: float = 0.0
    penalty: int = 0
    rank: int | None = None
    is_disqualified: bool = False
    format_data: dict[str, Any] = Field(default_factory=dict)


class OjContestParticipationUpdateRequest(OjContestParticipationCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class OjContestParticipationSchema(
    OjContestParticipationCreateRequest, OjContestParticipationTimestampSchema
):
    id: str
