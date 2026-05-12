from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel, ConfigDict
from core.enums import SoftDeleteEnum, ExportTypeEnum
from core.pojo import PageBounds
from core.pojo.datetime_mixin import DateTimeValidatorMixin


class BannerVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    title: str
    image: str
    category: str
    type: str
    position: str
    url: Optional[str] = None
    link_type: Optional[str] = "URL"
    summary: Optional[str] = None
    description: Optional[str] = None
    sort_code: Optional[int] = 0
    view_count: Optional[int] = 0
    click_count: Optional[int] = 0
    is_deleted: Optional[str] = SoftDeleteEnum.NO.value
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None


class BannerPageParam(PageBounds):
    pass


class BannerExportParam(BaseModel):
    export_type: str = ExportTypeEnum.CURRENT.value
    current: Optional[int] = None
    size: Optional[int] = None
    selected_id: Optional[str] = None

    @property
    def selected_ids(self) -> Optional[List[str]]:
        return self.selected_id.split(",") if self.selected_id else None


class BannerImportParam(BaseModel):
    data: List[BannerVO]
