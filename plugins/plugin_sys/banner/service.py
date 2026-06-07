"""
Banner service — standalone functions mirroring hei-gin's service.go pattern.
"""

from typing import Optional
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import update as sa_update, select
from fastapi import Request
from .models import SysBanner
from .params import BannerVO, BannerPageParam
from .dao import BannerDao
from core.utils import generate_id
from core.exception import BusinessException
from core.result import page_data, PageDataField
from core.auth import HeiAuthTool
import logging

logger = logging.getLogger(__name__)


def to_vo(entity: SysBanner) -> dict:
    """Entity → dict, matching Go's toVO()."""
    vo = {
        "id": entity.id,
        "title": entity.title,
        "image": entity.image,
        "link_type": entity.link_type,
        "category": entity.category,
        "type": entity.type,
        "position": entity.position,
        "sort_code": entity.sort_code,
        "view_count": entity.view_count,
        "click_count": entity.click_count,
    }
    if entity.url is not None:
        vo["url"] = entity.url
    if entity.summary is not None:
        vo["summary"] = entity.summary
    if entity.description is not None:
        vo["description"] = entity.description
    if entity.created_at is not None:
        vo["created_at"] = entity.created_at.strftime("%Y-%m-%d %H:%M:%S")
    if entity.created_by is not None:
        vo["created_by"] = entity.created_by
    if entity.updated_at is not None:
        vo["updated_at"] = entity.updated_at.strftime("%Y-%m-%d %H:%M:%S")
    if entity.updated_by is not None:
        vo["updated_by"] = entity.updated_by
    return vo


def page(db: Session, param: BannerPageParam) -> dict:
    dao = BannerDao(db)
    result = dao.find_page(param)
    records = [to_vo(r) for r in result.get("records", [])]
    return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)


def detail(db: Session, id: str) -> Optional[dict]:
    if not id:
        return None
    entity = BannerDao(db).find_by_id(id)
    if not entity:
        return None
    return to_vo(entity)


def create(db: Session, vo: BannerVO, user_id: Optional[str] = None) -> None:
    now = datetime.now()
    entity = SysBanner(
        id=generate_id(),
        title=vo.title,
        image=vo.image,
        link_type=vo.link_type,
        category=vo.category,
        type=vo.type,
        position=vo.position,
        sort_code=vo.sort_code or 0,
        view_count=vo.view_count or 0,
        click_count=vo.click_count or 0,
        created_at=now,
        updated_at=now,
    )
    if vo.url is not None:
        entity.url = vo.url
    if vo.summary is not None:
        entity.summary = vo.summary
    if vo.description is not None:
        entity.description = vo.description
    if user_id:
        entity.created_by = user_id
        entity.updated_by = user_id
    BannerDao(db).insert(entity)


def modify(db: Session, vo: BannerVO, user_id: Optional[str] = None) -> None:
    dao = BannerDao(db)
    entity = dao.find_by_id(vo.id)
    if not entity:
        raise BusinessException("数据不存在")
    now = datetime.now()
    up = {
        "title": vo.title,
        "image": vo.image,
        "link_type": vo.link_type,
        "category": vo.category,
        "type": vo.type,
        "position": vo.position,
        "sort_code": vo.sort_code,
        "view_count": vo.view_count,
        "click_count": vo.click_count,
        "updated_at": now,
    }
    if vo.url is not None:
        up["url"] = vo.url
    if vo.summary is not None:
        up["summary"] = vo.summary
    if vo.description is not None:
        up["description"] = vo.description
    if user_id:
        up["updated_by"] = user_id
    dao.db.execute(sa_update(SysBanner).where(SysBanner.id == vo.id).values(**up))
    dao.db.commit()


def remove(db: Session, ids: list) -> None:
    if not ids:
        return
    BannerDao(db).delete_by_ids(ids)


def options(db: Session) -> list:
    rows = db.execute(select(SysBanner).order_by(SysBanner.sort_code.asc())).scalars().all()
    return [to_vo(r) for r in rows]


# ═════════════════════════════════════════════════════════════════════
# Backward-compatible class — API handlers still use class style
# ═════════════════════════════════════════════════════════════════════

class BannerService:
    def __init__(self, db: Session):
        self.db = db
        self.dao = BannerDao(db)

    async def _get_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            return await HeiAuthTool.getLoginIdDefaultNull(request)
        except Exception:
            return None

    def page(self, param: BannerPageParam) -> dict:
        return page(self.db, param)

    def detail(self, id: str):
        return detail(self.db, id)

    async def create(self, vo: BannerVO, request: Optional[Request] = None) -> None:
        user_id = await self._get_user_id(request)
        return create(self.db, vo, user_id)

    async def modify(self, vo: BannerVO, request: Optional[Request] = None) -> None:
        user_id = await self._get_user_id(request)
        return modify(self.db, vo, user_id)

    def remove(self, ids: list) -> None:
        return remove(self.db, ids)

    def options(self) -> list:
        return options(self.db)
