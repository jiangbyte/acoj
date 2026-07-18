from datetime import datetime

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema


class OjProblemSampleAdminPageQuery(ApiSchema):
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


class OjProblemSampleTimestampSchema(ApiSchema):
    created_at: datetime | None = None
    created_by: str | None = None
    updated_at: datetime | None = None
    updated_by: str | None = None


class OjProblemSampleCreateRequest(ApiSchema):
    problem_id: str = Field(min_length=1, max_length=64)
    input: str | None = None
    output: str | None = None
    explanation: str | None = None
    sort: int = 0


class OjProblemSampleUpdateRequest(OjProblemSampleCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class OjProblemSampleSchema(OjProblemSampleCreateRequest, OjProblemSampleTimestampSchema):
    id: str
