from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema


class OjDatasetAdminPageQuery(ApiSchema):
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


class OjDatasetTimestampSchema(ApiSchema):
    created_at: datetime | None = None
    created_by: str | None = None
    updated_at: datetime | None = None
    updated_by: str | None = None


class OjDatasetCreateRequest(ApiSchema):
    problem_id: str = Field(min_length=1, max_length=64)
    name: str = Field(min_length=1, max_length=128)
    version: str = Field(min_length=1, max_length=64)
    is_active: bool = False
    data_zip_url: str | None = Field(default=None, max_length=1024)
    generator_url: str | None = Field(default=None, max_length=1024)
    checker: str | None = Field(default=None, max_length=64)
    checker_args: dict[str, Any] = Field(default_factory=dict)
    output_prefix: int | None = None
    output_limit: int | None = None
    unicode_enabled: bool | None = None
    extra: dict[str, Any] = Field(default_factory=dict)


class OjDatasetUpdateRequest(OjDatasetCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class OjDatasetSchema(OjDatasetCreateRequest, OjDatasetTimestampSchema):
    id: str
