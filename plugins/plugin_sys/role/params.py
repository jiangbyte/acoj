from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel, ConfigDict
from core.enums import DataScopeEnum
from core.pojo.datetime_mixin import DateTimeValidatorMixin
from .models import SysRole


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
    scope: str = DataScopeEnum.ALL.value
    custom_scope_group_ids: Optional[str] = None
    custom_scope_org_ids: Optional[str] = None


class GrantPermissionParam(BaseModel):
    role_id: str
    permissions: List[PermissionItem]


class ButtonPermissionScope(BaseModel):
    permission_code: str
    scope: str = DataScopeEnum.ALL.value
    custom_scope_group_ids: Optional[str] = None
    custom_scope_org_ids: Optional[str] = None


class GrantResourceParam(BaseModel):
    role_id: str
    resource_ids: List[str]
    permissions: List[ButtonPermissionScope] = []


def SysRoleToRoleVO(src: Optional[SysRole]) -> Optional[RoleVO]:
    if src is None:
        return None
    return RoleVO(
        id=src.id,
        code=src.code,
        name=src.name,
        category=src.category,
        description=src.description,
        status=src.status,
        sort_code=src.sort_code,
        extra=src.extra,
        created_at=src.created_at,
        created_by=src.created_by,
        updated_at=src.updated_at,
        updated_by=src.updated_by,
    )
