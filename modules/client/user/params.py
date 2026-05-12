from typing import Optional, List
from datetime import datetime, date
from pydantic import BaseModel, ConfigDict
from core.enums import SoftDeleteEnum, ExportTypeEnum
from core.pojo import PageBounds
from core.pojo.datetime_mixin import DateTimeValidatorMixin


class ClientUserVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    account: Optional[str] = None
    nickname: Optional[str] = None
    avatar: Optional[str] = None
    motto: Optional[str] = None
    gender: Optional[str] = None
    birthday: Optional[date] = None
    email: Optional[str] = None
    github: Optional[str] = None
    status: Optional[str] = None
    last_login_at: Optional[datetime] = None
    last_login_ip: Optional[str] = None
    login_count: Optional[int] = 0
    is_deleted: Optional[str] = SoftDeleteEnum.NO.value
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None


class ClientUserPageParam(PageBounds):
    pass


class ClientUserExportParam(BaseModel):
    export_type: str = ExportTypeEnum.CURRENT.value
    current: Optional[int] = None
    size: Optional[int] = None
    selected_id: Optional[str] = None

    @property
    def selected_ids(self) -> Optional[List[str]]:
        return self.selected_id.split(",") if self.selected_id else None


class ClientUserImportParam(BaseModel):
    data: List[ClientUserVO]
