from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema


class OjProblemAssetAdminPageQuery(ApiSchema):
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


class OjProblemAssetTimestampSchema(ApiSchema):
    created_at: datetime | None = None
    created_by: str | None = None
    updated_at: datetime | None = None
    updated_by: str | None = None


class OjProblemAssetCreateRequest(ApiSchema):
    problem_id: str = Field(min_length=1, max_length=64)
    asset_type: str = Field(min_length=1, max_length=32)
    name: str = Field(min_length=1, max_length=255)
    url: str | None = Field(default=None, max_length=1024)
    storage_key: str | None = Field(default=None, max_length=1024)
    checksum: str | None = Field(default=None, max_length=128)
    size: int | None = None
    version: str | None = Field(default=None, max_length=64)
    extra: dict[str, Any] = Field(default_factory=dict)


class OjProblemAssetUpdateRequest(OjProblemAssetCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class OjProblemAssetSchema(OjProblemAssetCreateRequest, OjProblemAssetTimestampSchema):
    id: str
