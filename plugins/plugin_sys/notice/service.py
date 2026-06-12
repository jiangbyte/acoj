"""Notice service — explicit field-by-field matching Go pattern."""

from typing import Optional, List
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import update as sa_update, select
from fastapi import Request
from .models import SysNotice
from .params import NoticeVO, NoticePageParam, NoticeLatestParam, SysNoticeToNoticeVO
from .repository import NoticeRepository
from core.utils import generate_id
from core.exception import BusinessException
from core.result import page_data, PageDataField
from core.auth import HeiAuthTool
import logging

logger = logging.getLogger(__name__)


def _parse_time(s: Optional[str]) -> Optional[datetime]:
    if not s:
        return None
    try:
        return datetime.strptime(s, "%Y-%m-%d %H:%M:%S")
    except ValueError:
        return None
def page(db: Session, param: NoticePageParam) -> dict:
    repository = NoticeRepository(db)
    result = repository.find_page(param)
    records = [SysNoticeToNoticeVO(r) for r in result.get("records", [])]
    return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)


def detail(db: Session, id: str) -> Optional[dict]:
    if not id:
        return None
    entity = NoticeRepository(db).find_by_id(id)
    if not entity:
        return None
    return SysNoticeToNoticeVO(entity)


def create(db: Session, vo: NoticeVO, user_id: Optional[str] = None) -> None:
    now = datetime.now()
    entity = SysNotice(
        id=generate_id(),
        title=vo.title,
        category=vo.category,
        type=vo.type,
        sort_code=vo.sort_code or 0,
        created_at=now,
        updated_at=now,
    )
    if vo.summary is not None:
        entity.summary = vo.summary
    if vo.content is not None:
        entity.content = vo.content
    if vo.cover is not None:
        entity.cover = vo.cover
    if vo.level:
        entity.level = vo.level
    if vo.status:
        entity.status = vo.status
    if vo.is_top:
        entity.is_top = vo.is_top
    if vo.author is not None:
        entity.author = vo.author
    if vo.publish_at is not None:
        entity.publish_at = _parse_time(vo.publish_at)
    if vo.expire_at is not None:
        entity.expire_at = _parse_time(vo.expire_at)
    if user_id:
        entity.created_by = user_id
        entity.updated_by = user_id
    NoticeRepository(db).insert(entity)


def modify(db: Session, vo: NoticeVO, user_id: Optional[str] = None) -> None:
    repository = NoticeRepository(db)
    entity = repository.find_by_id(vo.id)
    if not entity:
        raise BusinessException("数据不存在")
    now = datetime.now()
    up = {
        "title": vo.title,
        "category": vo.category,
        "type": vo.type,
        "sort_code": vo.sort_code,
        "updated_at": now,
    }
    if vo.summary is not None:
        up["summary"] = vo.summary
    if vo.content is not None:
        up["content"] = vo.content
    if vo.cover is not None:
        up["cover"] = vo.cover
    if vo.level:
        up["level"] = vo.level
    if vo.status:
        up["status"] = vo.status
    if vo.is_top:
        up["is_top"] = vo.is_top
    if vo.author is not None:
        up["author"] = vo.author
    if vo.publish_at is not None:
        up["publish_at"] = _parse_time(vo.publish_at)
    if vo.expire_at is not None:
        up["expire_at"] = _parse_time(vo.expire_at)
    if user_id:
        up["updated_by"] = user_id
    repository.db.execute(sa_update(SysNotice).where(SysNotice.id == vo.id).values(**up))
    repository.db.commit()


def remove(db: Session, ids: list) -> None:
    if not ids:
        return
    NoticeRepository(db).delete_by_ids(ids)


def options(db: Session) -> list:
    rows = db.execute(select(SysNotice).order_by(SysNotice.sort_code.asc())).scalars().all()
    return [SysNoticeToNoticeVO(r) for r in rows]


def latest(db: Session, param: NoticeLatestParam) -> list:
    """Return latest published notices."""
    repository = NoticeRepository(db)
    entities = repository.find_latest(param.size)
    return [SysNoticeToNoticeVO(r) for r in entities]


def public_page(db: Session, param: NoticePageParam) -> dict:
    """Paginate published notices — only ENABLED status."""
    repository = NoticeRepository(db)
    result = repository.find_public_page(param)
    records = [SysNoticeToNoticeVO(r) for r in result.get("records", [])]
    return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)


def public_detail(db: Session, id: str) -> Optional[dict]:
    """Return a published notice — only if ENABLED."""
    if not id:
        return None
    entity = NoticeRepository(db).find_public_by_id(id)
    if not entity:
        return None
    return SysNoticeToNoticeVO(entity)


class NoticeService:
    def __init__(self, db: Session):
        self.db = db
        self.repository = NoticeRepository(db)

    async def _get_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            return await HeiAuthTool.getLoginIdDefaultNull(request)
        except Exception:
            return None

    def page(self, param: NoticePageParam) -> dict:
        return page(self.db, param)

    def detail(self, id: str):
        return detail(self.db, id)

    async def create(self, vo: NoticeVO, request: Optional[Request] = None) -> None:
        return create(self.db, vo, await self._get_user_id(request))

    async def modify(self, vo: NoticeVO, request: Optional[Request] = None) -> None:
        return modify(self.db, vo, await self._get_user_id(request))

    def remove(self, ids: list) -> None:
        return remove(self.db, ids)

    def options(self) -> list:
        return options(self.db)

    def latest(self, param: NoticeLatestParam) -> list:
        return latest(self.db, param)

    def public_page(self, param: NoticePageParam) -> dict:
        return public_page(self.db, param)

    def public_detail(self, id: str):
        return public_detail(self.db, id)
