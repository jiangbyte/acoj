"""Home params — mirrors hei-gin plugin-sys/home/params.go."""

from typing import Optional, List
from pydantic import BaseModel, ConfigDict


class QuickActionVO(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    resource_id: str = ""
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


class HomeNotice(BaseModel):
    """Mirrors Go HomeNotice — created_at is a formatted string."""
    id: str = ""
    title: str = ""
    level: str = "NORMAL"
    created_at: str = ""


class HomeStats(BaseModel):
    total_users: int = 0


class HomeVO(BaseModel):
    quick_actions: List[QuickActionVO] = []
    available_resources: List[QuickActionVO] = []
    notices: List[HomeNotice] = []
    stats: HomeStats = HomeStats()
