from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel, ConfigDict
from core.pojo.datetime_mixin import DateTimeValidatorMixin


class QuickActionVO(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    resource_id: str
    parent_id: Optional[str] = None
    type: str = ""
    name: str = ""
    icon: str = ""
    route_path: str = ""
    sort_code: int = 0


class AddQuickActionParam(BaseModel):
    resource_id: str


class RemoveQuickActionParam(BaseModel):
    id: str


class SortQuickActionParam(BaseModel):
    ids: List[str]


class HomeNotice(DateTimeValidatorMixin, BaseModel):
    id: str
    title: str
    level: str = "NORMAL"
    created_at: Optional[datetime] = None


class HomeStats(BaseModel):
    total_users: int = 0


class HomeVO(BaseModel):
    quick_actions: List[QuickActionVO] = []
    available_resources: List[QuickActionVO] = []
    notices: List[HomeNotice] = []
    stats: HomeStats = HomeStats()
