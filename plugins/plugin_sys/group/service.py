"""Group service — class-based service with DI-friendly provider."""

from datetime import datetime
from typing import Optional

from fastapi import Depends
from sqlalchemy import select, update as sa_update
from sqlalchemy.orm import Session

from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from sdk.utils import generate_id
from sdk.utils.resolve_utils import resolve_name_path
from sdk.utils.tree_utils import build_tree, collect_descendant_ids
from sdk.web.exception import BusinessException
from sdk.web.result import map_page_data

from ..org.models import SysOrg
from ..user.models import SysUser
from .models import SysGroup
from .params import GroupPageParam, GroupTreeParam, GroupTreeVO, GroupVO, UnionTreeNode
from .repository import GroupRepository


def _normalize_parent_id(parent_id: Optional[str]) -> Optional[str]:
    return parent_id if parent_id not in (None, "", "0") else None


def _actor_user_id(actor: Optional[ActorContext]) -> Optional[str]:
    return actor.user_id if actor else None


def _tree_node(entity: SysGroup) -> GroupTreeVO:
    return GroupTreeVO.model_validate(entity)


def _enrich_vo(db: Session, vo: GroupVO) -> None:
    vo.org_names = resolve_name_path(vo.org_id, db, SysOrg)


def _batch_enrich(db: Session, vo_list: list[GroupVO]) -> None:
    for vo in vo_list:
        _enrich_vo(db, vo)


def _check_circular_parent(db: Session, entity_id: str, new_parent_id: Optional[str]) -> None:
    if not new_parent_id:
        return
    all_rows = db.execute(select(SysGroup)).scalars().all()
    parent_map = {row.id: row.parent_id for row in all_rows}
    current = new_parent_id
    while current:
        if current == entity_id:
            raise BusinessException("父级不能选择自身或子节点")
        current = parent_map.get(current)
        if not current or current == "0":
            break


def _collect_descendant_ids(db: Session, ids: list[str]) -> list[str]:
    all_rows = db.execute(select(SysGroup)).scalars().all()
    return collect_descendant_ids(
        all_rows,
        ids,
        get_id=lambda row: row.id,
        get_parent_id=lambda row: row.parent_id or "",
    )


class GroupService:
    def __init__(self, repository: GroupRepository):
        self.repository = repository
        self.db = repository.db

    def page(self, param: GroupPageParam) -> dict:
        page = map_page_data(
            self.repository.find_page_by_filters(param),
            GroupVO.model_validate,
            param.current,
            param.size,
        )
        _batch_enrich(self.db, page["records"])
        return page

    def detail(self, id: str) -> Optional[GroupVO]:
        if not id:
            return None
        entity = self.repository.find_by_id(id)
        if not entity:
            return None
        vo = GroupVO.model_validate(entity)
        _enrich_vo(self.db, vo)
        return vo

    def tree(self, param: GroupTreeParam) -> list[GroupTreeVO]:
        all_rows = self.db.execute(select(SysGroup).order_by(SysGroup.sort_code.asc())).scalars().all()
        filtered = all_rows
        if param.category:
            filtered = [row for row in filtered if row.category == param.category]
        nodes = [_tree_node(row) for row in filtered]
        return build_tree(
            nodes,
            get_id=lambda node: node.id or "",
            get_parent_id=lambda node: node.parent_id or "",
            get_children=lambda node: node.children,
            get_sort_code=lambda node: node.sort_code,
        )

    def union_tree(self) -> list[UnionTreeNode]:
        org_rows = self.db.execute(select(SysOrg).order_by(SysOrg.sort_code.asc())).scalars().all()
        group_rows = self.db.execute(select(SysGroup).order_by(SysGroup.sort_code.asc())).scalars().all()
        org_nodes: dict[str, UnionTreeNode] = {}
        for row in org_rows:
            node = UnionTreeNode(
                id=row.id,
                code=row.code,
                name=row.name,
                category=row.category,
                parent_id=row.parent_id,
                status=row.status,
                sort_code=row.sort_code,
                type="org",
            )
            org_nodes[row.id] = node
        group_nodes: dict[str, UnionTreeNode] = {}
        for row in group_rows:
            node = UnionTreeNode(
                id=row.id,
                code=row.code,
                name=row.name,
                category=row.category,
                parent_id=row.parent_id,
                org_id=row.org_id,
                status=row.status,
                sort_code=row.sort_code,
                type="group",
            )
            group_nodes[row.id] = node
        for node in group_nodes.values():
            parent_id = node.parent_id or ""
            if parent_id and parent_id in group_nodes:
                group_nodes[parent_id].children.append(node)
        orphan_groups: dict[str, list[UnionTreeNode]] = {}
        for node in group_nodes.values():
            parent_id = node.parent_id or ""
            if not parent_id or parent_id not in group_nodes:
                orphan_groups.setdefault(node.org_id or "", []).append(node)
        for org_id, node in org_nodes.items():
            if org_id in orphan_groups:
                node.children = orphan_groups[org_id] + node.children
        roots: list[UnionTreeNode] = []
        for node in org_nodes.values():
            parent_id = node.parent_id or ""
            if parent_id and parent_id in org_nodes:
                org_nodes[parent_id].children.append(node)
            else:
                roots.append(node)
        roots.sort(key=lambda item: item.sort_code or 0)
        return roots

    def create(self, vo: GroupVO, actor: Optional[ActorContext] = None) -> None:
        now = datetime.now()
        actor_user_id = _actor_user_id(actor)
        entity = SysGroup(
            id=generate_id(),
            code=vo.code,
            name=vo.name,
            category=vo.category,
            org_id=vo.org_id or "",
            parent_id=_normalize_parent_id(vo.parent_id),
            status=vo.status or "ENABLED",
            sort_code=vo.sort_code or 0,
            created_at=now,
            updated_at=now,
            description=vo.description,
            created_by=actor_user_id,
            updated_by=actor_user_id,
        )
        self.repository.insert(entity)

    def modify(self, vo: GroupVO, actor: Optional[ActorContext] = None) -> None:
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
            "org_id": vo.org_id or "",
            "parent_id": _normalize_parent_id(vo.parent_id),
            "status": vo.status,
            "sort_code": vo.sort_code,
            "updated_at": datetime.now(),
            "description": vo.description,
            "extra": vo.extra,
        }
        if actor_user_id:
            updates["updated_by"] = actor_user_id
        self.db.execute(sa_update(SysGroup).where(SysGroup.id == vo.id).values(**updates))
        self.db.commit()

    def remove(self, ids: list[str]) -> None:
        if not ids:
            return
        all_ids = _collect_descendant_ids(self.db, ids)
        self.db.execute(sa_update(SysUser).where(SysUser.group_id.in_(all_ids)).values(group_id=None))
        self.db.commit()
        self.repository.delete_by_ids(all_ids)

    def options(self) -> list[GroupVO]:
        rows = self.db.execute(select(SysGroup).order_by(SysGroup.sort_code.asc())).scalars().all()
        return [GroupVO.model_validate(row) for row in rows]


def get_group_service(db: Session = Depends(get_db)) -> GroupService:
    return GroupService(GroupRepository(db))
