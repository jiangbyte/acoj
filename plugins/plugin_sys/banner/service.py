"""
Banner service — standalone functions mirroring hei-gin's service.go pattern.
"""

from typing import Optional
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import update as sa_update, select
from fastapi import Request
from .models import SysBanner
from .params import BannerVO, BannerPageParam, SysBannerToBannerVO, BannerVOToSysBanner
from .repository import BannerRepository
from core.utils import generate_id
from core.exception import BusinessException
from core.result import page_data, PageDataField
from core.auth import HeiAuthTool
import logging

logger = logging.getLogger(__name__)


def page(db: Session, param: BannerPageParam) -> dict:
    repository = BannerRepository(db)
    result = repository.find_page(param)
    records = [SysBannerToBannerVO(r) for r in result.get("records", [])]
    return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)


def detail(db: Session, id: str) -> Optional[dict]:
    if not id:
        return None
    entity = BannerRepository(db).find_by_id(id)
    if not entity:
        return None
    return SysBannerToBannerVO(entity)


def create(db: Session, vo: BannerVO, user_id: Optional[str] = None) -> None:
    now = datetime.now()
    entity = BannerVOToSysBanner(vo)
    entity.id = generate_id()
    entity.created_at = now
    entity.updated_at = now
    if user_id:
        entity.created_by = user_id
        entity.updated_by = user_id
    BannerRepository(db).insert(entity)


def modify(db: Session, vo: BannerVO, user_id: Optional[str] = None) -> None:
    repository = BannerRepository(db)
    entity = repository.find_by_id(vo.id)
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
    repository.db.execute(sa_update(SysBanner).where(SysBanner.id == vo.id).values(**up))
    repository.db.commit()


def remove(db: Session, ids: list) -> None:
    if not ids:
        return
    BannerRepository(db).delete_by_ids(ids)


def options(db: Session) -> list:
    rows = db.execute(select(SysBanner).order_by(SysBanner.sort_code.asc())).scalars().all()
    return [SysBannerToBannerVO(r) for r in rows]


# ═════════════════════════════════════════════════════════════════════
# Backward-compatible class — API handlers still use class style
# ═════════════════════════════════════════════════════════════════════

class BannerService:
    def __init__(self, db: Session):
        self.db = db
        self.repository = BannerRepository(db)

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
