from datetime import datetime

from pydantic import Field

from app.core.config.enums import StatusEnum
from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema


class GroupCreateRequest(ApiSchema):
    name: str = Field(min_length=1, max_length=64)
    description: str | None = None
    status: StatusEnum = StatusEnum.ENABLED
    extra: dict = Field(default_factory=dict)


class GroupUpdateRequest(GroupCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class GroupAdminPageQuery(ApiSchema):
    pagination: PageQuery
    name: str | None = Field(default=None, max_length=64)
    status: str | None = Field(default=None, max_length=32)


class SysGroupSchema(ApiSchema):
    id: str
    name: str
    description: str | None = None
    status: str
    extra: dict
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class GroupRoleAssignRequest(ApiSchema):
    group_id: str
    role_id: str


class SysGroupRoleRelSchema(ApiSchema):
    id: str
    group_id: str
    role_id: str
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None
