from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel, ConfigDict, Field

from sdk.auth.enums import DataScope
from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin


class RoleVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    code: str
    name: str
    category: str
    description: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    extra: Optional[str] = None
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None


class RolePageParam(BaseModel):
    current: int = 1
    size: int = 10
    keyword: Optional[str] = None
    category: Optional[str] = None


class PermissionItem(BaseModel):
    permission_code: str
    scope: str = DataScope.ALL.value
    custom_scope_group_ids: Optional[str] = None
    custom_scope_org_ids: Optional[str] = None


class GrantPermissionParam(BaseModel):
    role_id: str
    permissions: List[PermissionItem]


class ButtonPermissionScope(BaseModel):
    permission_code: str
    scope: str = DataScope.ALL.value
    custom_scope_group_ids: Optional[str] = None
    custom_scope_org_ids: Optional[str] = None


class GrantResourceParam(BaseModel):
    role_id: str
    resource_ids: List[str]
    permissions: List[ButtonPermissionScope] = Field(default_factory=list)


class RefreshRoleSessionACLParam(BaseModel):
    role_id: str
