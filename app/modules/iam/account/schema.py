from datetime import datetime

from pydantic import Field

from app.core.config.enums import AccountStatusEnum, AccountType, DataScope
from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.iam.enums import AccountIdentityBindStatus, AccountIdentityType
from app.modules.iam.resource.schema import PermissionRegistryItem, ResourceGrantModuleOption


class AccountIdentitySchema(ApiSchema):
    id: str | None = None
    account_id: str | None = None
    identity_type: AccountIdentityType
    identifier: str = Field(min_length=1, max_length=128)
    verified: bool = False
    is_primary: bool = False
    bind_status: AccountIdentityBindStatus = AccountIdentityBindStatus.BOUND
    created_at: datetime | None = None
    created_by: str | None = None
    updated_at: datetime | None = None
    updated_by: str | None = None


class AccountIdentityUpsertPayload(ApiSchema):
    account_id: str
    identity_type: AccountIdentityType
    identifier: str = Field(min_length=1, max_length=128)
    verified: bool = False
    is_primary: bool = False
    bind_status: AccountIdentityBindStatus = AccountIdentityBindStatus.BOUND


class AccountCreateRequest(ApiSchema):
    account: str = Field(min_length=3, max_length=64)
    password: str = Field(min_length=6, max_length=128)
    account_type: AccountType
    account_status: AccountStatusEnum = AccountStatusEnum.ENABLED
    name: str = Field(min_length=1, max_length=64)
    nickname: str | None = Field(default=None, max_length=64)
    avatar: str | None = None
    signature: str | None = None
    phone: str | None = None
    email: str | None = None
    email_identity: str | None = Field(default=None, max_length=128)
    phone_identity: str | None = Field(default=None, max_length=32)
    email_identity_verified: bool = False
    phone_identity_verified: bool = False
    email_identity_bind_status: AccountIdentityBindStatus = AccountIdentityBindStatus.BOUND
    phone_identity_bind_status: AccountIdentityBindStatus = AccountIdentityBindStatus.BOUND
    employee_no: str | None = Field(default=None, max_length=64)
    title: str | None = Field(default=None, max_length=64)
    remark: str | None = None


class AccountUpdateRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    account: str = Field(min_length=3, max_length=64)
    password: str | None = Field(default=None, min_length=6, max_length=128)
    account_type: AccountType
    account_status: AccountStatusEnum = AccountStatusEnum.ENABLED
    name: str = Field(min_length=1, max_length=64)
    nickname: str | None = Field(default=None, max_length=64)
    avatar: str | None = None
    signature: str | None = None
    phone: str | None = None
    email: str | None = None
    email_identity: str | None = Field(default=None, max_length=128)
    phone_identity: str | None = Field(default=None, max_length=32)
    email_identity_verified: bool = False
    phone_identity_verified: bool = False
    email_identity_bind_status: AccountIdentityBindStatus = AccountIdentityBindStatus.BOUND
    phone_identity_bind_status: AccountIdentityBindStatus = AccountIdentityBindStatus.BOUND
    employee_no: str | None = Field(default=None, max_length=64)
    title: str | None = Field(default=None, max_length=64)
    remark: str | None = None


class AccountCancelPayload(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    cancel_reason: str | None = None


class AccountAdminPageQuery(ApiSchema):
    pagination: PageQuery
    account: str | None = Field(default=None, max_length=64)
    name: str | None = Field(default=None, max_length=64)
    phone: str | None = Field(default=None, max_length=32)
    email: str | None = Field(default=None, max_length=128)
    account_type: AccountType | None = None
    account_status: AccountStatusEnum | None = None


class SysAccountSchema(ApiSchema):
    id: str
    account: str
    account_type: AccountType
    account_status: AccountStatusEnum
    name: str
    nickname: str | None = None
    avatar: str | None = None
    signature: str | None = None
    phone: str | None = None
    email: str | None = None
    email_identity: str | None = None
    phone_identity: str | None = None
    email_identity_verified: bool = False
    phone_identity_verified: bool = False
    email_identity_bind_status: AccountIdentityBindStatus | None = None
    phone_identity_bind_status: AccountIdentityBindStatus | None = None
    identities: list[AccountIdentitySchema] = Field(default_factory=list)
    employee_no: str | None = None
    title: str | None = None
    remark: str | None = None
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


class AccountRoleOption(ApiSchema):
    id: str
    code: str
    name: str
    status: str


class AccountGroupOption(ApiSchema):
    id: str
    name: str
    status: str


class AccountDeptGrantInfo(ApiSchema):
    dept_id: str
    is_primary: bool = False


class AccountResourceGrantInfo(ApiSchema):
    resource_id: str = Field(min_length=1, max_length=64)
    permission_keys: list[str] = Field(default_factory=list)


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


class AccountPermissionGrantInfo(ApiSchema):
    permission_key: str = Field(min_length=1, max_length=128)
    data_scope: DataScope = DataScope.SELF
    custom_scope_dept_ids: list[str] = Field(default_factory=list)


class AccountOwnPermissionResponse(ApiSchema):
    id: str
    grant_info_list: list[AccountPermissionGrantInfo] = Field(default_factory=list)


class AccountGrantPermissionRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    grant_info_list: list[AccountPermissionGrantInfo] = Field(default_factory=list)


class AccountOwnPermissionDetailResponse(ApiSchema):
    id: str
    permissions: list[PermissionRegistryItem] = Field(default_factory=list)
    grant_info_list: list[AccountPermissionGrantInfo] = Field(default_factory=list)


class AccountOwnResourceResponse(ApiSchema):
    id: str
    modules: list[ResourceGrantModuleOption] = Field(default_factory=list)
    grant_info_list: list[AccountResourceGrantInfo] = Field(default_factory=list)


class AccountGrantResourceRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    grant_info_list: list[AccountResourceGrantInfo] = Field(default_factory=list)


class AccountOwnRoleResponse(ApiSchema):
    id: str
    roles: list[AccountRoleOption] = Field(default_factory=list)
    role_ids: list[str] = Field(default_factory=list)


class AccountGrantRoleRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    role_ids: list[str] = Field(default_factory=list)


class AccountOwnGroupResponse(ApiSchema):
    id: str
    groups: list[AccountGroupOption] = Field(default_factory=list)
    group_ids: list[str] = Field(default_factory=list)


class AccountGrantGroupRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    group_ids: list[str] = Field(default_factory=list)


class AccountOwnDeptResponse(ApiSchema):
    id: str
    grant_info_list: list[AccountDeptGrantInfo] = Field(default_factory=list)


class AccountGrantDeptRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    grant_info_list: list[AccountDeptGrantInfo] = Field(default_factory=list)
