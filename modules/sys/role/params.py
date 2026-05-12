from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel, ConfigDict
from core.enums import SoftDeleteEnum, ExportTypeEnum, DataScopeEnum
from core.pojo import PageBounds
from core.pojo.datetime_mixin import DateTimeValidatorMixin


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
    is_deleted: Optional[str] = SoftDeleteEnum.NO.value
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None


class RolePageParam(PageBounds):
    pass


class RoleExportParam(BaseModel):
    export_type: str = ExportTypeEnum.CURRENT.value
    current: Optional[int] = None
    size: Optional[int] = None
    selected_id: Optional[str] = None

    @property
    def selected_ids(self) -> Optional[List[str]]:
        return self.selected_id.split(",") if self.selected_id else None


class RoleImportParam(BaseModel):
    data: List[RoleVO]


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
