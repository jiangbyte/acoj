from datetime import datetime

from pydantic import Field

from app.core.config.enums import DataScope, ResourceType, StatusEnum
from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema


class ResourceCreateRequest(ApiSchema):
    code: str = Field(min_length=1, max_length=64)
    name: str = Field(min_length=1, max_length=64)
    resource_type: ResourceType
    parent_id: str | None = Field(default=None, max_length=64)
    module: str | None = Field(default=None, max_length=64)
    path: str | None = Field(default=None, max_length=255)
    component: str | None = Field(default=None, max_length=255)
    redirect: str | None = Field(default=None, max_length=255)
    icon: str | None = Field(default=None, max_length=255)
    href: str | None = Field(default=None, max_length=255)
    sort: int = 99
    is_visible: bool = True
    is_cache: bool = False
    is_affix: bool = False
    status: StatusEnum = StatusEnum.ENABLED
    description: str | None = None
    extra: dict = Field(default_factory=dict)


class ResourceUpdateRequest(ResourceCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class ResourceAdminPageQuery(ApiSchema):
    pagination: PageQuery
    code: str | None = Field(default=None, max_length=64)
    name: str | None = Field(default=None, max_length=64)
    resource_type: ResourceType | None = None
    module: str | None = Field(default=None, max_length=64)
    parent_id: str | None = Field(default=None, max_length=64)
    status: str | None = Field(default=None, max_length=32)


class SysResourceSchema(ApiSchema):
    id: str
    parent_id: str | None = None
    code: str
    name: str
    resource_type: ResourceType
    module: str | None = None
    path: str | None = None
    component: str | None = None
    redirect: str | None = None
    icon: str | None = None
    href: str | None = None
    sort: int
    is_visible: bool
    is_cache: bool
    is_affix: bool
    status: str
    description: str | None = None
    extra: dict
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class ResourcePermissionBindRequest(ApiSchema):
    resource_id: str
    permission_key: str
    data_scope: DataScope = DataScope.SELF
    custom_scope_dept_ids: list[str] = Field(default_factory=list)
    sort: int = 99
    description: str | None = None


class SysResourcePermissionRelSchema(ApiSchema):
    id: str
    resource_id: str
    permission_key: str
    data_scope: DataScope
    custom_scope_dept_ids: list[str]
    sort: int
    status: str
    description: str | None = None
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class ResourceTreeNode(ApiSchema):
    id: str
    code: str
    name: str
    resource_type: ResourceType
    children: list["ResourceTreeNode"] = Field(default_factory=list)
