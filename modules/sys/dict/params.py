from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel, ConfigDict
from core.enums import SoftDeleteEnum, ExportTypeEnum
from core.pojo import PageBounds
from core.pojo.datetime_mixin import DateTimeValidatorMixin


class DictVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    code: str
    label: Optional[str] = None
    value: Optional[str] = None
    color: Optional[str] = None
    category: Optional[str] = None
    parent_id: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    is_deleted: Optional[str] = SoftDeleteEnum.NO.value
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None


class DictTreeVO(BaseModel):
    """Dict tree node with children"""
    id: Optional[str] = None
    code: Optional[str] = None
    label: Optional[str] = None
    value: Optional[str] = None
    color: Optional[str] = None
    category: Optional[str] = None
    parent_id: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    children: List["DictTreeVO"] = []


class DictPageParam(PageBounds):
    parent_id: Optional[str] = None
    category: Optional[str] = None
    keyword: Optional[str] = None


class DictListParam(BaseModel):
    parent_id: Optional[str] = None
    category: Optional[str] = None


class DictTreeParam(BaseModel):
    category: Optional[str] = None


class DictExportParam(BaseModel):
    export_type: str = ExportTypeEnum.CURRENT.value
    current: Optional[int] = None
    size: Optional[int] = None
    selected_id: Optional[str] = None

    @property
    def selected_ids(self) -> Optional[List[str]]:
        if self.selected_id:
            return self.selected_id.split(",")
        return None


class DictImportParam(BaseModel):
    data: List[DictVO]
