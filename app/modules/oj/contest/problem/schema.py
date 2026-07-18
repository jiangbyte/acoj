from datetime import datetime

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema


class OjContestProblemAdminPageQuery(ApiSchema):
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


class OjContestProblemTimestampSchema(ApiSchema):
    created_at: datetime | None = None
    created_by: str | None = None
    updated_at: datetime | None = None
    updated_by: str | None = None


class OjContestProblemCreateRequest(ApiSchema):
    contest_id: str = Field(min_length=1, max_length=64)
    problem_id: str = Field(min_length=1, max_length=64)
    label: str | None = Field(default=None, max_length=32)
    points: float = 100.0
    partial: bool = True
    is_pretest: bool = False
    max_submissions: int | None = None
    sort: int = 0


class OjContestProblemUpdateRequest(OjContestProblemCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class OjContestProblemSchema(OjContestProblemCreateRequest, OjContestProblemTimestampSchema):
    id: str
