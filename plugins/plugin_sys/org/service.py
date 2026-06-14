"""Org service — class-based service with DI-friendly provider."""

from datetime import datetime
from typing import Optional

from fastapi import Depends
from sqlalchemy import func, select, update as sa_update
from sqlalchemy.orm import Session

from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from sdk.utils import generate_id
from sdk.utils.tree_utils import build_tree, collect_descendant_ids
from sdk.web.exception import BusinessException
from sdk.web.result import map_page_data

from ..group.models import SysGroup
from ..position.models import SysPosition
from ..user.models import SysUser
from .models import SysOrg
from .params import OrgPageParam, OrgTreeParam, OrgTreeVO, OrgVO
from .repository import OrgRepository


def _normalize_parent_id(parent_id: Optional[str]) -> Optional[str]:
    return parent_id if parent_id not in (None, "", "0") else None


def _actor_user_id(actor: Optional[ActorContext]) -> Optional[str]:
    return actor.user_id if actor else None


def _check_circular_parent(db: Session, entity_id: str, new_parent_id: Optional[str]) -> None:
    if not new_parent_id:
        return
    all_rows = db.execute(select(SysOrg)).scalars().all()
    parent_map = {row.id: row.parent_id for row in all_rows}
    current = new_parent_id
    while current:
        if current == entity_id:
            raise BusinessException("父级不能选择自身或子节点")
        current = parent_map.get(current)
        if not current or current == "0":
            break


def _collect_descendant_ids(db: Session, ids: list[str]) -> list[str]:
    all_rows = db.execute(select(SysOrg)).scalars().all()
    return collect_descendant_ids(
        all_rows,
        ids,
        get_id=lambda row: row.id,
        get_parent_id=lambda row: row.parent_id or "",
    )


class OrgService:
    def __init__(self, repository: OrgRepository):
        self.repository = repository
        self.db = repository.db

    def page(self, param: OrgPageParam) -> dict:
        return map_page_data(
            self.repository.find_page_by_filters(param),
            OrgVO.model_validate,
            param.current,
            param.size,
        )

    def detail(self, id: str) -> Optional[OrgVO]:
        if not id:
            return None
        entity = self.repository.find_by_id(id)
        if not entity:
            return None
        return OrgVO.model_validate(entity)

    def tree(self, param: OrgTreeParam) -> list[OrgTreeVO]:
        rows = self.db.execute(select(SysOrg).order_by(SysOrg.sort_code.asc())).scalars().all()
        if param.category:
            rows = [row for row in rows if row.category == param.category]
        nodes = [OrgTreeVO.model_validate(row) for row in rows]
        return build_tree(
            nodes,
            get_id=lambda node: node.id or "",
            get_parent_id=lambda node: node.parent_id or "",
            get_children=lambda node: node.children,
            get_sort_code=lambda node: node.sort_code,
        )

    def create(self, vo: OrgVO, actor: Optional[ActorContext] = None) -> None:
        now = datetime.now()
        actor_user_id = _actor_user_id(actor)
        entity = SysOrg(
            id=generate_id(),
            code=vo.code,
            name=vo.name,
            category=vo.category or "",
            status="ENABLED",
            sort_code=vo.sort_code or 0,
            created_at=now,
            updated_at=now,
            parent_id=_normalize_parent_id(vo.parent_id),
            description=vo.description,
            extra=vo.extra,
            created_by=actor_user_id,
            updated_by=actor_user_id,
        )
        self.repository.insert(entity)

    def modify(self, vo: OrgVO, actor: Optional[ActorContext] = None) -> None:
        entity = self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        if vo.parent_id is not None and vo.parent_id != entity.parent_id:
            _check_circular_parent(self.db, vo.id, vo.parent_id)
        actor_user_id = _actor_user_id(actor)
        updates = {
            "code": vo.code,
            "name": vo.name,
            "category": vo.category,
            "sort_code": vo.sort_code,
            "updated_at": datetime.now(),
            "parent_id": _normalize_parent_id(vo.parent_id),
            "description": vo.description,
            "extra": vo.extra,
        }
        if actor_user_id:
            updates["updated_by"] = actor_user_id
        self.db.execute(sa_update(SysOrg).where(SysOrg.id == vo.id).values(**updates))
        self.db.commit()

    def remove(self, ids: list[str]) -> None:
        if not ids:
            return
        all_ids = _collect_descendant_ids(self.db, ids)
        user_count = (
            self.db.execute(
                select(func.count()).select_from(SysUser).where(SysUser.org_id.in_(all_ids))
            ).scalar()
            or 0
        )
        if user_count > 0:
            raise BusinessException("组织存在关联用户，无法删除")
        group_count = (
            self.db.execute(
                select(func.count()).select_from(SysGroup).where(SysGroup.org_id.in_(all_ids))
            ).scalar()
            or 0
        )
        if group_count > 0:
            raise BusinessException("组织存在关联用户组，无法删除")
        position_count = (
            self.db.execute(
                select(func.count()).select_from(SysPosition).where(SysPosition.org_id.in_(all_ids))
            ).scalar()
            or 0
        )
        if position_count > 0:
            raise BusinessException("组织存在关联职位，无法删除")
        self.repository.delete_by_ids(all_ids)

    def options(self) -> list[OrgVO]:
        rows = self.db.execute(select(SysOrg).order_by(SysOrg.sort_code.asc())).scalars().all()
        return [OrgVO.model_validate(row) for row in rows]


def get_org_service(db: Session = Depends(get_db)) -> OrgService:
    return OrgService(OrgRepository(db))
