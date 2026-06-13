"""Position service — class-based service with DI-friendly provider."""

from typing import Optional, List
from datetime import datetime
from fastapi import Depends
from sqlalchemy.orm import Session

from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from . import SysPosition
from .params import PositionVO, PositionPageParam, SysPositionToPositionVO
from .repository import PositionRepository
from sdk.utils import generate_id
from sdk.web.exception import BusinessException
from sdk.web.result import page_data
from sdk.utils.resolve_utils import resolve_name_path
import logging

logger = logging.getLogger(__name__)
def _enrich_vo(db: Session, vo: dict) -> None:
    from ..org.models import SysOrg
    from ..group.models import SysGroup
    vo["org_names"] = resolve_name_path(vo.get("org_id"), db, SysOrg)
    vo["group_names"] = resolve_name_path(vo.get("group_id"), db, SysGroup)


def _batch_enrich(db: Session, vo_list: List[dict]) -> None:
    for vo in vo_list:
        _enrich_vo(db, vo)
class PositionService:
    def __init__(self, repository: PositionRepository):
        self.repository = repository
        self.db = repository.db

    @classmethod
    def from_db(cls, db: Session) -> "PositionService":
        return cls(PositionRepository(db))

    def page(self, param: PositionPageParam) -> dict:
        result = self.repository.find_page_by_filters(param)
        records = [SysPositionToPositionVO(r) for r in result.get("records", [])]
        _batch_enrich(self.db, records)
        return page_data(records=records, total=result["total"], page=param.current, size=param.size)

    def detail(self, id: str) -> Optional[PositionVO]:
        if not id:
            return None
        entity = self.repository.find_by_id(id)
        if not entity:
            return None
        vo = SysPositionToPositionVO(entity)
        _enrich_vo(self.db, vo)
        return vo

    def create(self, vo: PositionVO, actor: Optional[ActorContext] = None) -> None:
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
        if actor and actor.user_id:
            entity.created_by = actor.user_id
            entity.updated_by = actor.user_id
        self.repository.insert(entity)

    def modify(self, vo: PositionVO, actor: Optional[ActorContext] = None) -> None:
        entity = self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        up = {
            "code": vo.code,
            "name": vo.name,
            "category": vo.category,
            "org_id": vo.org_id,
            "group_id": vo.group_id,
            "status": vo.status,
            "sort_code": vo.sort_code,
            "updated_at": datetime.now(),
            "description": vo.description if vo.description is not None else None,
            "extra": vo.extra if vo.extra is not None else None,
        }
        if actor and actor.user_id:
            up["updated_by"] = actor.user_id
        self.repository.update_by_id(vo.id, up)

    def remove(self, ids: list[str]) -> None:
        if not ids:
            return
        self.repository.clear_user_positions(ids)
        self.repository.delete_by_ids(ids)

    def options(self) -> list:
        return [SysPositionToPositionVO(r) for r in self.repository.find_all_ordered()]


def get_position_service(db: Session = Depends(get_db)) -> PositionService:
    return PositionService.from_db(db)
