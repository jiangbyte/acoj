from typing import Optional, List
from pydantic import BaseModel
from core.enums import ExportTypeEnum


class PageBounds(BaseModel):
    current: int = 1
    size: int = 10

    @property
    def offset(self) -> int:
        return (self.current - 1) * self.size


class IdParam(BaseModel):
    id: str


class IdsParam(BaseModel):
    ids: List[str]


class BaseExportParam(BaseModel):
    export_type: str = ExportTypeEnum.CURRENT.value
    current: Optional[int] = None
    size: Optional[int] = None
    selected_id: Optional[str] = None

    @property
    def selected_ids(self) -> Optional[List[str]]:
        return self.selected_id.split(",") if self.selected_id else None
