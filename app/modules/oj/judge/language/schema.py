from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.config.enums import StatusEnum
from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema


class OjLanguageAdminPageQuery(ApiSchema):
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


class OjLanguageTimestampSchema(ApiSchema):
    created_at: datetime | None = None
    created_by: str | None = None
    updated_at: datetime | None = None
    updated_by: str | None = None


class OjLanguageCreateRequest(ApiSchema):
    key: str = Field(min_length=1, max_length=32)
    name: str = Field(min_length=1, max_length=64)
    short_name: str | None = Field(default=None, max_length=32)
    common_name: str | None = Field(default=None, max_length=32)
    ace_mode: str | None = Field(default=None, max_length=64)
    pygments: str | None = Field(default=None, max_length=64)
    extension: str | None = Field(default=None, max_length=32)
    template: str | None = None
    compile_command: str | None = None
    run_command: str | None = None
    status: StatusEnum = StatusEnum.ENABLED
    extra: dict[str, Any] = Field(default_factory=dict)


class OjLanguageUpdateRequest(OjLanguageCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class OjLanguageSchema(OjLanguageCreateRequest, OjLanguageTimestampSchema):
    id: str
