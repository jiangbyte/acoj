from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel, ConfigDict
from core.enums import SoftDeleteEnum, ExportTypeEnum
from core.pojo import PageBounds
from core.pojo.datetime_mixin import DateTimeValidatorMixin


class NoticeVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    title: str
    category: str
    type: str
    summary: Optional[str] = None
    content: Optional[str] = None
    cover: Optional[str] = None
    level: Optional[str] = None
    view_count: Optional[int] = 0
    is_top: Optional[str] = None
    position: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    is_deleted: Optional[str] = SoftDeleteEnum.NO.value
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None


class NoticePageParam(PageBounds):
    pass


class NoticeExportParam(BaseModel):
    export_type: str = ExportTypeEnum.CURRENT.value
    current: Optional[int] = None
    size: Optional[int] = None
    selected_id: Optional[str] = None

    @property
    def selected_ids(self) -> Optional[List[str]]:
        return self.selected_id.split(",") if self.selected_id else None


class NoticeImportParam(BaseModel):
    data: List[NoticeVO]
