from datetime import datetime

from pydantic import Field

from app.core.config.enums import StatusEnum
from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema


class PositionCreateRequest(ApiSchema):
    name: str = Field(min_length=1, max_length=64)
    code: str = Field(min_length=1, max_length=64)
    category: str = Field(min_length=1, max_length=32)
    owner_dept_id: str | None = Field(default=None, max_length=64)
    sort: int = 99
    is_virtual: bool = False
    status: StatusEnum = StatusEnum.ENABLED
    description: str | None = None
    extra: dict = Field(default_factory=dict)


class PositionUpdateRequest(PositionCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class PositionAdminPageQuery(ApiSchema):
    pagination: PageQuery
    name: str | None = Field(default=None, max_length=64)
    code: str | None = Field(default=None, max_length=64)
    category: str | None = Field(default=None, max_length=32)
    status: str | None = Field(default=None, max_length=32)


class SysPositionSchema(ApiSchema):
    id: str
    name: str
    code: str
    category: str
    owner_dept_id: str | None = None
    sort: int
    is_virtual: bool
    status: str
    description: str | None = None
    extra: dict
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None
