from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel, ConfigDict
from core.enums import SoftDeleteEnum, ExportTypeEnum
from core.pojo import PageBounds
from core.pojo.datetime_mixin import DateTimeValidatorMixin


class PositionVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    code: str
    name: str
    category: str
    org_id: Optional[str] = None
    group_id: Optional[str] = None
    description: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    extra: Optional[str] = None
    is_deleted: Optional[str] = SoftDeleteEnum.NO.value
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None


class PositionPageParam(PageBounds):
    keyword: Optional[str] = None
    group_id: Optional[str] = None
    org_id: Optional[str] = None


class PositionExportParam(BaseModel):
    export_type: str = ExportTypeEnum.CURRENT.value
    current: Optional[int] = None
    size: Optional[int] = None
    selected_id: Optional[str] = None

    @property
    def selected_ids(self) -> Optional[List[str]]:
        return self.selected_id.split(",") if self.selected_id else None


class PositionImportParam(BaseModel):
    data: List[PositionVO]
