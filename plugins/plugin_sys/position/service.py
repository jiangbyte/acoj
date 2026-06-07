"""Position service — explicit field-by-field matching Go pattern."""

from typing import Optional, List
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import update as sa_update, select, func
from fastapi import Request
from . import SysPosition
from .params import PositionVO, PositionPageParam
from .dao import PositionDao
from core.utils import generate_id
from core.exception import BusinessException
from core.result import page_data, PageDataField
from core.auth import HeiAuthTool
from core.utils.resolve_utils import resolve_name_path
from ..user.models import SysUser
import logging

logger = logging.getLogger(__name__)


def to_vo(entity: SysPosition) -> dict:
    vo = {
        "id": entity.id,
        "code": entity.code,
        "name": entity.name,
        "category": entity.category,
        "org_id": entity.org_id,
        "group_id": entity.group_id,
        "status": entity.status,
        "sort_code": entity.sort_code,
    }
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


def page(db: Session, param: PositionPageParam) -> dict:
    if not param.group_id:
        return page_data(records=[], total=0, page=param.current, size=param.size)
    dao = PositionDao(db)
    result = dao.find_page_by_filters(param)
    records = [to_vo(r) for r in result.get("records", [])]
    _batch_enrich(db, records)
    return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)


def detail(db: Session, id: str) -> Optional[dict]:
    if not id:
        return None
    entity = PositionDao(db).find_by_id(id)
    if not entity:
        return None
    vo = to_vo(entity)
    _enrich_vo(db, vo)
    return vo


def _enrich_vo(db: Session, vo: dict) -> None:
    from ..org.models import SysOrg
    from ..group.models import SysGroup
    vo["org_names"] = resolve_name_path(vo.get("org_id"), db, SysOrg)
    vo["group_names"] = resolve_name_path(vo.get("group_id"), db, SysGroup)


def _batch_enrich(db: Session, vo_list: List[dict]) -> None:
    for vo in vo_list:
        _enrich_vo(db, vo)


def create(db: Session, vo: PositionVO, user_id: Optional[str] = None) -> None:
    now = datetime.now()
    entity = SysPosition(
        id=generate_id(),
        code=vo.code,
        name=vo.name,
        category=vo.category,
        org_id=vo.org_id or "",
        group_id=vo.group_id or "",
        status=vo.status or "ENABLED",
        sort_code=vo.sort_code or 0,
        created_at=now,
        updated_at=now,
    )
    if vo.description is not None:
        entity.description = vo.description
    if user_id:
        entity.created_by = user_id
        entity.updated_by = user_id
    PositionDao(db).insert(entity)


def modify(db: Session, vo: PositionVO, user_id: Optional[str] = None) -> None:
    dao = PositionDao(db)
    entity = dao.find_by_id(vo.id)
    if not entity:
        raise BusinessException("数据不存在")
    now = datetime.now()
    up = {
        "code": vo.code,
        "name": vo.name,
        "category": vo.category,
        "org_id": vo.org_id or "",
        "group_id": vo.group_id or "",
        "status": vo.status,
        "sort_code": vo.sort_code,
        "updated_at": now,
    }
    if vo.description is not None:
        up["description"] = vo.description
    if user_id:
        up["updated_by"] = user_id
    dao.db.execute(sa_update(SysPosition).where(SysPosition.id == vo.id).values(**up))
    dao.db.commit()


def remove(db: Session, ids: list) -> None:


def options(db: Session) -> list:
    rows = db.execute(select(SysPosition).order_by(SysPosition.sort_code.asc())).scalars().all()
    return [to_vo(r) for r in rows]
    if not ids:
        return
    dao = PositionDao(db)
    cnt = db.execute(select(func.count()).select_from(SysUser).where(SysUser.position_id.in_(ids))).scalar() or 0
    if cnt > 0:
        raise BusinessException("职位存在关联用户，无法删除")
    dao.delete_by_ids(ids)


class PositionService:
    def __init__(self, db: Session):
        self.db = db
        self.dao = PositionDao(db)

    async def _get_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            return await HeiAuthTool.getLoginIdDefaultNull(request)
        except Exception:
            return None

    def page(self, param: PositionPageParam) -> dict:
        return page(self.db, param)

    def detail(self, id: str):
        return detail(self.db, id)

    async def create(self, vo: PositionVO, request: Optional[Request] = None) -> None:
        return create(self.db, vo, await self._get_user_id(request))

    async def modify(self, vo: PositionVO, request: Optional[Request] = None) -> None:
        return modify(self.db, vo, await self._get_user_id(request))

    def remove(self, ids: list) -> None:
        return remove(self.db, ids)

def options(db: Session) -> list:
    rows = db.execute(select(SysPosition).order_by(SysPosition.sort_code.asc())).scalars().all()
    return [to_vo(r) for r in rows]
