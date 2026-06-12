from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel, ConfigDict
from core.pojo.datetime_mixin import DateTimeValidatorMixin
from .models import SysNotice


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
    is_top: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    author: Optional[str] = None
    publish_at: Optional[str] = None
    expire_at: Optional[str] = None
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None


class NoticePageParam(BaseModel):
    current: int = 1
    size: int = 10
    keyword: Optional[str] = None
    category: Optional[str] = None
    status: Optional[str] = None


class NoticeLatestParam(BaseModel):
    """Latest notices param — mirrors hei-gin's NoticeLatestParam."""
    size: int = 5


def SysNoticeToNoticeVO(src: Optional[SysNotice]) -> Optional[NoticeVO]:
    if src is None:
        return None
    return NoticeVO(
        id=src.id,
        title=src.title,
        category=src.category,
        type=src.type,
        summary=src.summary,
        content=src.content,
        cover=src.cover,
        level=src.level,
        is_top=src.is_top,
        status=src.status,
        sort_code=src.sort_code,
        author=src.author,
        publish_at=src.publish_at.strftime("%Y-%m-%d %H:%M:%S") if src.publish_at else None,
        expire_at=src.expire_at.strftime("%Y-%m-%d %H:%M:%S") if src.expire_at else None,
        created_at=src.created_at,
        created_by=src.created_by,
        updated_at=src.updated_at,
        updated_by=src.updated_by,
    )
