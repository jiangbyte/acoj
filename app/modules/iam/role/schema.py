from datetime import datetime

from pydantic import Field

from app.core.config.enums import DataScope, StatusEnum
from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.iam.enums import RoleScopeType
from app.modules.iam.schema import (
    PermissionRegistryItem,
    ResourceGrantModuleOption,
    SysAccountSchema,
)


class RoleCreateRequest(ApiSchema):
    code: str = Field(min_length=1, max_length=64)
    name: str = Field(min_length=1, max_length=64)
    category: str = Field(min_length=1, max_length=64)
    scope_type: RoleScopeType = RoleScopeType.PLATFORM
    owner_dept_id: str | None = Field(default=None, max_length=64)
    sort: int = 99
    status: StatusEnum = StatusEnum.ENABLED
    is_builtin: bool = False
    description: str | None = None
    extra: dict = Field(default_factory=dict)


class RoleUpdateRequest(RoleCreateRequest):
    id: str = Field(min_length=1, max_length=64)


class RoleAdminPageQuery(ApiSchema):
    pagination: PageQuery
    code: str | None = Field(default=None, max_length=64)
    name: str | None = Field(default=None, max_length=64)
    category: str | None = Field(default=None, max_length=64)
    scope_type: RoleScopeType | None = None
    status: str | None = Field(default=None, max_length=32)


class SysRoleSchema(ApiSchema):
    id: str
    code: str
    name: str
    category: str
    scope_type: RoleScopeType
    owner_dept_id: str | None = None
    sort: int
    status: str
    is_builtin: bool
    description: str | None = None
    extra: dict
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class RolePermissionGrantInfo(ApiSchema):
    permission_key: str = Field(min_length=1, max_length=128)
    data_scope: DataScope = DataScope.SELF
    custom_scope_dept_ids: list[str] = Field(default_factory=list)


class RoleOwnPermissionResponse(ApiSchema):
    id: str
    grant_info_list: list[RolePermissionGrantInfo] = Field(default_factory=list)


class RoleGrantPermissionRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    grant_info_list: list[RolePermissionGrantInfo] = Field(default_factory=list)


class RoleResourceGrantInfo(ApiSchema):
    resource_id: str = Field(min_length=1, max_length=64)
    permission_keys: list[str] = Field(default_factory=list)


class RoleOwnResourceResponse(ApiSchema):
    id: str
    modules: list[ResourceGrantModuleOption] = Field(default_factory=list)
    grant_info_list: list[RoleResourceGrantInfo] = Field(default_factory=list)


class RoleGrantResourceRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    grant_info_list: list[RoleResourceGrantInfo] = Field(default_factory=list)


class RoleOwnPermissionDetailResponse(ApiSchema):
    id: str
    permissions: list[PermissionRegistryItem] = Field(default_factory=list)
    grant_info_list: list[RolePermissionGrantInfo] = Field(default_factory=list)


class RoleOwnUserResponse(ApiSchema):
    id: str
    users: list[SysAccountSchema] = Field(default_factory=list)
    account_ids: list[str] = Field(default_factory=list)


class RoleGrantUserRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    account_ids: list[str] = Field(default_factory=list)
