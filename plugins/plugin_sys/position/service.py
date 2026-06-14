"""Position service — class-based service with DI-friendly provider."""

from datetime import datetime
from typing import List, Optional

from fastapi import Depends
from sqlalchemy.orm import Session

from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from sdk.utils import generate_id
from sdk.utils.resolve_utils import resolve_name_path
from sdk.web.exception import BusinessException
from sdk.web.result import map_page_data

from .models import SysPosition
from .params import PositionPageParam, PositionVO
from .repository import PositionRepository


def _actor_user_id(actor: Optional[ActorContext]) -> Optional[str]:
    return actor.user_id if actor else None


def _enrich_vo(db: Session, vo: PositionVO) -> None:
    from ..org.models import SysOrg
    from ..group.models import SysGroup
    vo.org_names = resolve_name_path(vo.org_id, db, SysOrg)
    vo.group_names = resolve_name_path(vo.group_id, db, SysGroup)


def _batch_enrich(db: Session, vo_list: List[PositionVO]) -> None:
    for vo in vo_list:
        _enrich_vo(db, vo)


class PositionService:
    def __init__(self, repository: PositionRepository):
        self.repository = repository
        self.db = repository.db

    def page(self, param: PositionPageParam) -> dict:
        page = map_page_data(
            self.repository.find_page_by_filters(param),
            PositionVO.from_entity,
            param.current,
            param.size,
        )
        _batch_enrich(self.db, page["records"])
        return page

    def detail(self, id: str) -> Optional[PositionVO]:
        if not id:
            return None
        entity = self.repository.find_by_id(id)
        if not entity:
            return None
        vo = PositionVO.model_validate(entity)
        _enrich_vo(self.db, vo)
        return vo

    def create(self, vo: PositionVO, actor: Optional[ActorContext] = None) -> None:
        now = datetime.now()
        actor_user_id = _actor_user_id(actor)
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
            description=vo.description,
        )
        if actor_user_id:
            entity.created_by = actor_user_id
            entity.updated_by = actor_user_id
        self.repository.insert(entity)

    def modify(self, vo: PositionVO, actor: Optional[ActorContext] = None) -> None:
        entity = self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        actor_user_id = _actor_user_id(actor)
        up = {
            "code": vo.code,
            "name": vo.name,
            "category": vo.category,
            "org_id": vo.org_id,
            "group_id": vo.group_id,
            "status": vo.status,
            "sort_code": vo.sort_code,
            "updated_at": datetime.now(),
            "description": vo.description,
            "extra": vo.extra,
        }
        if actor_user_id:
            up["updated_by"] = actor_user_id
        self.repository.update_by_id(vo.id, up)

    def remove(self, ids: list[str]) -> None:
        if not ids:
            return
        self.repository.clear_user_positions(ids)
        self.repository.delete_by_ids(ids)

    def options(self) -> list:
        return [PositionVO.model_validate(row) for row in self.repository.find_all_ordered()]


def get_position_service(db: Session = Depends(get_db)) -> PositionService:
    return PositionService(PositionRepository(db))
