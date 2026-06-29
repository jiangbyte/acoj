from datetime import datetime

from pydantic import Field

from app.core.config.enums import DataScope, StatusEnum
from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.iam.account.schema import AccountRoleOption, SysAccountSchema
from app.modules.iam.resource.schema import PermissionRegistryItem, ResourceGrantModuleOption


class GroupCreateRequest(ApiSchema):
    name: str = Field(min_length=1, max_length=64)
    owner_dept_id: str | None = Field(default=None, max_length=64)
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
    owner_dept_id: str | None = None
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


class GroupResourceGrantInfo(ApiSchema):
    resource_id: str = Field(min_length=1, max_length=64)
    permission_keys: list[str] = Field(default_factory=list)


class GroupPermissionGrantInfo(ApiSchema):
    permission_key: str = Field(min_length=1, max_length=128)
    data_scope: DataScope = DataScope.SELF
    custom_scope_dept_ids: list[str] = Field(default_factory=list)


class GroupOwnUserResponse(ApiSchema):
    id: str
    users: list[SysAccountSchema] = Field(default_factory=list)
    account_ids: list[str] = Field(default_factory=list)


class GroupGrantUserRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    account_ids: list[str] = Field(default_factory=list)


class GroupOwnRoleResponse(ApiSchema):
    id: str
    roles: list[AccountRoleOption] = Field(default_factory=list)
    role_ids: list[str] = Field(default_factory=list)


class GroupGrantRoleRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    role_ids: list[str] = Field(default_factory=list)


class GroupOwnResourceResponse(ApiSchema):
    id: str
    modules: list[ResourceGrantModuleOption] = Field(default_factory=list)
    grant_info_list: list[GroupResourceGrantInfo] = Field(default_factory=list)


class GroupGrantResourceRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    grant_info_list: list[GroupResourceGrantInfo] = Field(default_factory=list)


class GroupOwnPermissionResponse(ApiSchema):
    id: str
    grant_info_list: list[GroupPermissionGrantInfo] = Field(default_factory=list)


class GroupOwnPermissionDetailResponse(ApiSchema):
    id: str
    permissions: list[PermissionRegistryItem] = Field(default_factory=list)
    grant_info_list: list[GroupPermissionGrantInfo] = Field(default_factory=list)


class GroupGrantPermissionRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    grant_info_list: list[GroupPermissionGrantInfo] = Field(default_factory=list)
