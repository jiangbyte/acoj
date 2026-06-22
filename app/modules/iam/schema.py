from datetime import datetime

from pydantic import Field

from app.core.config.enums import (
    AccountStatusEnum,
    DataScope,
    GrantEffect,
    GrantMode,
    GrantSubjectType,
    LoginScope,
    ResourceType,
    RoleScopeType,
    UserType,
)
from app.core.schema.base import ApiSchema


class AccountCreateRequest(ApiSchema):
    account: str = Field(min_length=3, max_length=64)
    password: str = Field(min_length=6, max_length=128)
    account_type: UserType
    name: str = Field(min_length=1, max_length=64)
    nickname: str | None = Field(default=None, max_length=64)
    avatar: str | None = None
    signature: str | None = None
    phone: str | None = None
    email: str | None = None


class SysAccountSchema(ApiSchema):
    id: str
    account: str
    account_type: UserType
    account_status: AccountStatusEnum
    name: str
    nickname: str | None = None
    avatar: str | None = None
    signature: str | None = None
    phone: str | None = None
    email: str | None = None
    is_superuser: bool
    cancelled_at: datetime | None = Field(default=None, examples=["2026-06-18T12:00:00Z"])
    cancelled_by: str | None = None
    cancel_reason: str | None = None
    last_login_ip: str | None = None
    last_login_address: str | None = None
    last_login_time: datetime | None = None
    last_login_device: str | None = None
    latest_login_ip: str | None = None
    latest_login_address: str | None = None
    latest_login_time: datetime | None = None
    latest_login_device: str | None = None
    created_at: datetime = Field(examples=["2026-06-18T12:00:00Z"])
    created_by: str | None = None
    updated_at: datetime = Field(examples=["2026-06-18T12:00:00Z"])
    updated_by: str | None = None


class DeptCreateRequest(ApiSchema):
    name: str
    code: str
    category: str
    parent_id: str | None = None
    master_id: str | None = None
    deputy_master_id: str | None = None
    sort: int = 99
    is_virtual: bool = False
    extra: dict = Field(default_factory=dict)


class SysDeptSchema(ApiSchema):
    id: str
    parent_id: str | None = None
    master_id: str | None = None
    deputy_master_id: str | None = None
    name: str
    code: str
    category: str
    sort: int
    is_virtual: bool
    status: str
    extra: dict
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class GroupCreateRequest(ApiSchema):
    name: str
    description: str | None = None
    extra: dict = Field(default_factory=dict)


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


class RoleCreateRequest(ApiSchema):
    code: str
    name: str
    category: str
    scope_type: RoleScopeType
    owner_dept_id: str | None = None
    sort: int = 99
    is_builtin: bool = False
    description: str | None = None
    extra: dict = Field(default_factory=dict)


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


class ResourceCreateRequest(ApiSchema):
    code: str
    name: str
    resource_type: ResourceType
    parent_id: str | None = None
    module: str | None = None
    path: str | None = None
    component: str | None = None
    redirect: str | None = None
    icon: str | None = None
    href: str | None = None
    sort: int = 99
    is_visible: bool = True
    is_cache: bool = False
    is_affix: bool = False
    description: str | None = None
    extra: dict = Field(default_factory=dict)


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


class SubjectResourceGrantRequest(ApiSchema):
    subject_type: GrantSubjectType
    subject_id: str
    resource_id: str
    grant_mode: GrantMode = GrantMode.CASCADE
    effect: GrantEffect = GrantEffect.ALLOW
    description: str | None = None
    expired_at: datetime | None = None


class SubjectPermissionGrantRequest(ApiSchema):
    subject_type: GrantSubjectType
    subject_id: str
    permission_key: str
    data_scope: DataScope = DataScope.SELF
    custom_scope_dept_ids: list[str] = Field(default_factory=list)
    effect: GrantEffect = GrantEffect.ALLOW
    description: str | None = None
    reason: str | None = None
    expired_at: datetime | None = None


class AccountRoleAssignRequest(ApiSchema):
    account_id: str
    role_id: str


class AccountGroupAssignRequest(ApiSchema):
    account_id: str
    group_id: str


class AccountDeptAssignRequest(ApiSchema):
    account_id: str
    dept_id: str
    is_primary: bool = False


class GroupRoleAssignRequest(ApiSchema):
    group_id: str
    role_id: str


class SysSubjectResourceGrantRelSchema(ApiSchema):
    id: str
    subject_type: GrantSubjectType
    subject_id: str
    resource_id: str
    grant_mode: GrantMode
    effect: GrantEffect
    status: str
    description: str | None = None
    expired_at: datetime | None = None
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class SysSubjectPermissionGrantRelSchema(ApiSchema):
    id: str
    subject_type: GrantSubjectType
    subject_id: str
    permission_key: str
    data_scope: DataScope
    custom_scope_dept_ids: list[str]
    effect: GrantEffect
    status: str
    description: str | None = None
    reason: str | None = None
    expired_at: datetime | None = None
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class SysAccountRoleRelSchema(ApiSchema):
    id: str
    account_id: str
    role_id: str
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class SysAccountGroupRelSchema(ApiSchema):
    id: str
    account_id: str
    group_id: str
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class SysAccountDeptRelSchema(ApiSchema):
    id: str
    account_id: str
    dept_id: str
    is_primary: bool
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class SysGroupRoleRelSchema(ApiSchema):
    id: str
    group_id: str
    role_id: str
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


class DeptTreeNode(ApiSchema):
    id: str
    name: str
    code: str
    category: str
    children: list["DeptTreeNode"] = Field(default_factory=list)


class PermissionGrantResponse(ApiSchema):
    permission_key: str
    data_scope: DataScope
    custom_scope_dept_ids: list[str]
    source_type: GrantSubjectType | str
    source_id: str


class PermissionRegistryRouteResponse(ApiSchema):
    path: str
    methods: list[str] = Field(default_factory=list)
    login_scopes: list[LoginScope | str] = Field(default_factory=list)


class PermissionRegistryResponse(ApiSchema):
    permission_key: str
    module: str
    source: str
    methods: list[str] = Field(default_factory=list)
    login_scopes: list[LoginScope | str] = Field(default_factory=list)
    routes: list[PermissionRegistryRouteResponse] = Field(default_factory=list)
