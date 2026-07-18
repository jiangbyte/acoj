from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.config.enums import StatusEnum
from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.oj.enums import (
    OjContestFormat,
    OjContestVisibility,
    OjScoreboardVisibility,
)


class OjContestAdminPageQuery(ApiSchema):
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


class OjContestTimestampSchema(ApiSchema):
    created_at: datetime | None = None
    created_by: str | None = None
    updated_at: datetime | None = None
    updated_by: str | None = None


class OjContestCreateRequest(ApiSchema):
    key: str = Field(min_length=1, max_length=64)
    name: str = Field(min_length=1, max_length=255)
    description: str | None = None
    summary: str | None = Field(default=None, max_length=500)
    start_at: datetime
    end_at: datetime
    duration_seconds: int | None = None
    visibility: OjContestVisibility = OjContestVisibility.PUBLIC
    contest_format: OjContestFormat = OjContestFormat.ICPC
    format_config: dict[str, Any] = Field(default_factory=dict)
    scoreboard_visibility: OjScoreboardVisibility = OjScoreboardVisibility.VISIBLE
    is_rated: bool = False
    rating_floor: int | None = None
    rating_ceiling: int | None = None
    access_code_hash: str | None = Field(default=None, max_length=255)
    allow_virtual: bool = False
    freeze_at: datetime | None = None
    unfreeze_at: datetime | None = None
    status: StatusEnum = StatusEnum.ENABLED
    extra: dict[str, Any] = Field(default_factory=dict)


class OjContestUpdateRequest(OjContestCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class OjContestSchema(OjContestCreateRequest, OjContestTimestampSchema):
    id: str
