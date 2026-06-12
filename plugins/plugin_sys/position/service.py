"""Position service — explicit field-by-field matching Go pattern."""

from typing import Optional, List
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import update as sa_update, select, delete as sa_delete
from fastapi import Request
from . import SysPosition
from .params import PositionVO, PositionPageParam, SysPositionToPositionVO
from .repository import PositionRepository
from core.utils import generate_id
from core.exception import BusinessException
from core.result import page_data, PageDataField
from core.auth import HeiAuthTool
from core.utils.resolve_utils import resolve_name_path
from ..user.models import SysUser
import logging

logger = logging.getLogger(__name__)
def page(db: Session, param: PositionPageParam) -> dict:
    repository = PositionRepository(db)
    result = repository.find_page_by_filters(param)
    records = [SysPositionToPositionVO(r) for r in result.get("records", [])]
    _batch_enrich(db, records)
    return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)


def detail(db: Session, id: str) -> Optional[dict]:
    if not id:
        return None
    entity = PositionRepository(db).find_by_id(id)
    if not entity:
        return None
    vo = SysPositionToPositionVO(entity)
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
        org_id=vo.org_id,
        group_id=vo.group_id,
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
    PositionRepository(db).insert(entity)


def modify(db: Session, vo: PositionVO, user_id: Optional[str] = None) -> None:
    repository = PositionRepository(db)
    entity = repository.find_by_id(vo.id)
    if not entity:
        raise BusinessException("数据不存在")
    now = datetime.now()
    up = {
        "code": vo.code,
        "name": vo.name,
        "category": vo.category,
        "org_id": vo.org_id,
        "group_id": vo.group_id,
        "status": vo.status,
        "sort_code": vo.sort_code,
        "updated_at": now,
    }
    if vo.description is not None:
        up["description"] = vo.description
    else:
        up["description"] = None
    if vo.extra is not None:
        up["extra"] = vo.extra
    else:
        up["extra"] = None
    if user_id:
        up["updated_by"] = user_id
    repository.db.execute(sa_update(SysPosition).where(SysPosition.id == vo.id).values(**up))
    repository.db.commit()


def remove(db: Session, ids: list) -> None:
    if not ids:
        return
    db.execute(sa_update(SysUser).where(SysUser.position_id.in_(ids)).values(position_id=None))
    PositionRepository(db).delete_by_ids(ids)


class PositionService:
    def __init__(self, db: Session):
        self.db = db
        self.repository = PositionRepository(db)

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
    return [SysPositionToPositionVO(r) for r in rows]
