"""Org service — class-based service with DI-friendly provider."""

from datetime import datetime
from typing import Optional

from fastapi import Depends
from sqlalchemy import func, select, update as sa_update
from sqlalchemy.orm import Session

from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from sdk.utils import generate_id
from sdk.web.exception import BusinessException
from sdk.web.result import PageDataField, page_data

from ..group.models import SysGroup
from ..position.models import SysPosition
from ..user.models import SysUser
from .models import SysOrg
from .params import OrgPageParam, OrgTreeParam, OrgVO, SysOrgToOrgTreeVO, SysOrgToOrgVO
from .repository import OrgRepository


def _sort_tree(nodes: list[dict]) -> None:
    nodes.sort(key=lambda item: item.get("sort_code", 0) or 0)
    for node in nodes:
        children = node.get("children")
        if children:
            _sort_tree(children)


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
    children_map: dict[str, list[str]] = {}
    for row in all_rows:
        children_map.setdefault(row.parent_id or "", []).append(row.id)
    all_ids = set(ids)
    stack = list(ids)
    while stack:
        parent_id = stack.pop()
        for child_id in children_map.get(parent_id, []):
            if child_id not in all_ids:
                all_ids.add(child_id)
                stack.append(child_id)
    return list(all_ids)


class OrgService:
    def __init__(self, repository: OrgRepository):
        self.repository = repository
        self.db = repository.db

    @classmethod
    def from_db(cls, db: Session) -> "OrgService":
        return cls(OrgRepository(db))

    def page(self, param: OrgPageParam) -> dict:
        result = self.repository.find_page_by_filters(param)
        records = [SysOrgToOrgVO(row) for row in result.get("records", [])]
        return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)

    def detail(self, id: str) -> Optional[dict]:
        if not id:
            return None
        entity = self.repository.find_by_id(id)
        if not entity:
            return None
        return SysOrgToOrgVO(entity)

    def tree(self, param: OrgTreeParam) -> list[dict]:
        rows = self.db.execute(select(SysOrg).order_by(SysOrg.sort_code.asc())).scalars().all()
        if param.category:
            rows = [row for row in rows if row.category == param.category]
        node_map = {row.id: SysOrgToOrgTreeVO(row).model_dump() for row in rows}
        roots = []
        for node in node_map.values():
            parent_id = node.get("parent_id") or ""
            if parent_id and parent_id in node_map:
                node_map[parent_id]["children"].append(node)
            else:
                roots.append(node)
        _sort_tree(roots)
        return roots

    def create(self, vo: OrgVO, actor: Optional[ActorContext] = None) -> None:
        now = datetime.now()
        entity = SysOrg(
            id=generate_id(),
            code=vo.code,
            name=vo.name,
            category=vo.category or "",
            status="ENABLED",
            sort_code=vo.sort_code or 0,
            created_at=now,
            updated_at=now,
        )
        if vo.parent_id not in (None, "", "0"):
            entity.parent_id = vo.parent_id
        if vo.description is not None:
            entity.description = vo.description
        if vo.extra is not None:
            entity.extra = vo.extra
        if actor and actor.user_id:
            entity.created_by = actor.user_id
            entity.updated_by = actor.user_id
        self.repository.insert(entity)

    def modify(self, vo: OrgVO, actor: Optional[ActorContext] = None) -> None:
        entity = self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        if vo.parent_id is not None and vo.parent_id != entity.parent_id:
            _check_circular_parent(self.db, vo.id, vo.parent_id)
        updates = {
            "code": vo.code,
            "name": vo.name,
            "category": vo.category,
            "sort_code": vo.sort_code,
            "updated_at": datetime.now(),
            "parent_id": vo.parent_id if vo.parent_id not in (None, "", "0") else None,
            "description": vo.description if vo.description is not None else None,
            "extra": vo.extra if vo.extra is not None else None,
        }
        if actor and actor.user_id:
            updates["updated_by"] = actor.user_id
        self.db.execute(sa_update(SysOrg).where(SysOrg.id == vo.id).values(**updates))
        self.db.commit()

    def remove(self, ids: list[str]) -> None:
        if not ids:
            return
        all_ids = _collect_descendant_ids(self.db, ids)
        user_count = self.db.execute(select(func.count()).select_from(SysUser).where(SysUser.org_id.in_(all_ids))).scalar() or 0
        if user_count > 0:
            raise BusinessException("组织存在关联用户，无法删除")
        group_count = self.db.execute(select(func.count()).select_from(SysGroup).where(SysGroup.org_id.in_(all_ids))).scalar() or 0
        if group_count > 0:
            raise BusinessException("组织存在关联用户组，无法删除")
        position_count = self.db.execute(select(func.count()).select_from(SysPosition).where(SysPosition.org_id.in_(all_ids))).scalar() or 0
        if position_count > 0:
            raise BusinessException("组织存在关联职位，无法删除")
        self.repository.delete_by_ids(all_ids)

    def options(self) -> list[dict]:
        rows = self.db.execute(select(SysOrg).order_by(SysOrg.sort_code.asc())).scalars().all()
        return [SysOrgToOrgVO(row) for row in rows]


def get_org_service(db: Session = Depends(get_db)) -> OrgService:
    return OrgService.from_db(db)
