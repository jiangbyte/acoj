"""Contest registration schemas."""

from datetime import datetime

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.biz.contest.enums import ContestRegistrationSource, ContestRegistrationStatus


class OjContestRegistrationSchema(ApiSchema):
    id: str
    contest_id: str
    account_id: str
    status: ContestRegistrationStatus
    source: ContestRegistrationSource
    applied_at: datetime
    reviewed_at: datetime | None = None
    reviewed_by: str | None = None
    remark: str | None = None
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class OjContestRegistrationAdminPageQuery(ApiSchema):
    pagination: PageQuery
    contest_id: str | None = None
    account_id: str | None = None
    status: ContestRegistrationStatus | None = None


class OjContestRegistrationAddRequest(ApiSchema):
    account_id: str = Field(min_length=1, max_length=64)
    remark: str | None = None


class OjContestRegistrationIdsRequest(ApiSchema):
    ids: list[str] = Field(min_length=1)
    remark: str | None = None


class OjContestRegistrationRejectRequest(OjContestRegistrationIdsRequest):
    remark: str | None = None
