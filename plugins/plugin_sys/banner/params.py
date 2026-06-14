from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict

from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin


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
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None


class BannerPageParam(BaseModel):
    current: int = 1
    size: int = 10
