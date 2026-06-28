from datetime import datetime

from pydantic import Field

from app.core.config.enums import AccountStatusEnum, AccountType, DataScope
from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema


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
