"""Home params — mirrors hei-gin plugin-sys/home/params.go."""

from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel, ConfigDict, Field

from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin


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


class HomeNotice(DateTimeValidatorMixin, BaseModel):
    """Mirrors Go HomeNotice — created_at is a formatted string."""
    id: str = ""
    title: str = ""
    level: str = "NORMAL"
    created_at: Optional[datetime] = None


class HomeStats(BaseModel):
    total_users: int = 0


class HomeVO(BaseModel):
    quick_actions: List[QuickActionVO] = Field(default_factory=list)
    available_resources: List[QuickActionVO] = Field(default_factory=list)
    notices: List[HomeNotice] = Field(default_factory=list)
    stats: HomeStats = Field(default_factory=HomeStats)
