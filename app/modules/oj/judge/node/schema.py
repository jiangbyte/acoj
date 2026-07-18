from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.oj.enums import (
    OjJudgeNodeStatus,
)


class OjJudgeNodeAdminPageQuery(ApiSchema):
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


class OjJudgeNodeTimestampSchema(ApiSchema):
    created_at: datetime | None = None
    created_by: str | None = None
    updated_at: datetime | None = None
    updated_by: str | None = None


class OjJudgeNodeCreateRequest(ApiSchema):
    name: str = Field(min_length=1, max_length=128)
    auth_key_hash: str = Field(min_length=1, max_length=255)
    status: OjJudgeNodeStatus = OjJudgeNodeStatus.ENABLED
    online: bool = False
    tier: int = 1
    last_ip: str | None = Field(default=None, max_length=64)
    last_heartbeat_at: datetime | None = None
    load: float | None = None
    supported_languages: list[str] = Field(default_factory=list)
    supported_modes: list[str] = Field(default_factory=list)
    description: str | None = None
    extra: dict[str, Any] = Field(default_factory=dict)


class OjJudgeNodeUpdateRequest(OjJudgeNodeCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class OjJudgeNodeSchema(OjJudgeNodeCreateRequest, OjJudgeNodeTimestampSchema):
    id: str
