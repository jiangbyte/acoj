from typing import Optional
from datetime import datetime
from pydantic import BaseModel, ConfigDict
from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin
from .models import SysBanner


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


def SysBannerToBannerVO(src: Optional[SysBanner]) -> Optional[BannerVO]:
    if src is None:
        return None
    return BannerVO(
        id=src.id,
        title=src.title,
        image=src.image,
        url=src.url,
        link_type=src.link_type,
        summary=src.summary,
        description=src.description,
        category=src.category,
        type=src.type,
        position=src.position,
        sort_code=src.sort_code,
        view_count=src.view_count,
        click_count=src.click_count,
        created_at=src.created_at,
        created_by=src.created_by,
        updated_at=src.updated_at,
        updated_by=src.updated_by,
    )


def BannerVOToSysBanner(src: Optional[BannerVO]) -> Optional[SysBanner]:
    if src is None:
        return None
    dst = SysBanner(
        id=src.id or "",
        title=src.title,
        image=src.image,
        category=src.category,
        type=src.type,
        position=src.position,
        link_type=src.link_type or "URL",
        sort_code=src.sort_code or 0,
        view_count=src.view_count or 0,
        click_count=src.click_count or 0,
    )
    dst.url = src.url
    dst.summary = src.summary
    dst.description = src.description
    dst.created_by = src.created_by
    dst.updated_by = src.updated_by
    return dst
