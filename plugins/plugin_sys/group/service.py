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
from sdk.web.exception import BusinessException
from sdk.web.result import PageDataField, page_data

from ..org.models import SysOrg
from ..org.params import OrgVO
from ..user.models import SysUser
from . import SysGroup
from .params import GroupPageParam, GroupTreeParam, GroupVO, SysGroupToGroupTreeVO, SysGroupToGroupVO
from .repository import GroupRepository


def _tree_node(entity: SysGroup) -> dict:
    return SysGroupToGroupTreeVO(entity).model_dump()


def _build_tree(node: dict, parent: SysGroup, children_map: dict) -> None:
    children = children_map.get(parent.id, [])
    if not children:
        return
    child_nodes = []
    for child in children:
        child_node = _tree_node(child)
        _build_tree(child_node, child, children_map)
        child_nodes.append(child_node)
    node["children"] = child_nodes


def _sort_tree(nodes: list[dict]) -> None:
    nodes.sort(key=lambda item: item.get("sort_code", 0) or 0)
    for node in nodes:
        children = node.get("children")
        if children:
            _sort_tree(children)


def _enrich_vo(db: Session, vo: dict) -> None:
    vo["org_names"] = resolve_name_path(vo.get("org_id"), db, SysOrg)


def _batch_enrich(db: Session, vo_list: list[dict]) -> None:
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


class GroupService:
    def __init__(self, repository: GroupRepository):
        self.repository = repository
        self.db = repository.db

    @classmethod
    def from_db(cls, db: Session) -> "GroupService":
        return cls(GroupRepository(db))

    def page(self, param: GroupPageParam) -> dict:
        result = self.repository.find_page_by_filters(param)
        records = [SysGroupToGroupVO(row) for row in result.get("records", [])]
        _batch_enrich(self.db, records)
        return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)

    def detail(self, id: str) -> Optional[dict]:
        if not id:
            return None
        entity = self.repository.find_by_id(id)
        if not entity:
            return None
        vo = SysGroupToGroupVO(entity)
        _enrich_vo(self.db, vo)
        return vo

    def tree(self, param: GroupTreeParam) -> list[dict]:
        all_rows = self.db.execute(select(SysGroup).order_by(SysGroup.sort_code.asc())).scalars().all()
        filtered = all_rows
        if param.category:
            filtered = [row for row in filtered if row.category == param.category]
        children_map: dict[str, list[SysGroup]] = {}
        for row in filtered:
            children_map.setdefault(row.parent_id or "", []).append(row)
        roots = children_map.get("", [])
        result = []
        for row in roots:
            node = _tree_node(row)
            _build_tree(node, row, children_map)
            result.append(node)
        _sort_tree(result)
        return result

    def union_tree(self) -> list[dict]:
        org_rows = self.db.execute(select(SysOrg).order_by(SysOrg.sort_code.asc())).scalars().all()
        group_rows = self.db.execute(select(SysGroup).order_by(SysGroup.sort_code.asc())).scalars().all()
        org_nodes: dict[str, dict] = {}
        for row in org_rows:
            node = OrgVO.model_validate(row).model_dump()
            node["_type"] = "org"
            node["children"] = []
            org_nodes[row.id] = node
        group_nodes: dict[str, dict] = {}
        for row in group_rows:
            node = SysGroupToGroupVO(row)
            node["_type"] = "group"
            node["children"] = []
            group_nodes[row.id] = node
        for node in group_nodes.values():
            parent_id = node.get("parent_id") or ""
            if parent_id and parent_id in group_nodes:
                group_nodes[parent_id]["children"].append(node)
        orphan_groups: dict[str, list[dict]] = {}
        for node in group_nodes.values():
            parent_id = node.get("parent_id") or ""
            if not parent_id or parent_id not in group_nodes:
                orphan_groups.setdefault(node.get("org_id") or "", []).append(node)
        for org_id, node in org_nodes.items():
            if org_id in orphan_groups:
                node["children"] = orphan_groups[org_id] + node["children"]
        roots = []
        for node in org_nodes.values():
            parent_id = node.get("parent_id") or ""
            if parent_id and parent_id in org_nodes:
                org_nodes[parent_id]["children"].append(node)
            else:
                roots.append(node)
        _sort_tree(roots)
        return roots

    def create(self, vo: GroupVO, actor: Optional[ActorContext] = None) -> None:
        now = datetime.now()
        entity = SysGroup(
            id=generate_id(),
            code=vo.code,
            name=vo.name,
            category=vo.category,
            org_id=vo.org_id or "",
            parent_id=vo.parent_id,
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

    def modify(self, vo: GroupVO, actor: Optional[ActorContext] = None) -> None:
        entity = self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        if vo.parent_id is not None and vo.parent_id != entity.parent_id:
            _check_circular_parent(self.db, vo.id, vo.parent_id)
        updates = {
            "code": vo.code,
            "name": vo.name,
            "category": vo.category,
            "org_id": vo.org_id or "",
            "parent_id": vo.parent_id,
            "status": vo.status,
            "sort_code": vo.sort_code,
            "updated_at": datetime.now(),
            "description": vo.description if vo.description is not None else None,
            "extra": vo.extra if vo.extra is not None else None,
        }
        if actor and actor.user_id:
            updates["updated_by"] = actor.user_id
        self.db.execute(sa_update(SysGroup).where(SysGroup.id == vo.id).values(**updates))
        self.db.commit()

    def remove(self, ids: list[str]) -> None:
        if not ids:
            return
        all_ids = _collect_descendant_ids(self.db, ids)
        self.db.execute(sa_update(SysUser).where(SysUser.group_id.in_(all_ids)).values(group_id=None))
        self.db.commit()
        self.repository.delete_by_ids(all_ids)

    def options(self) -> list[dict]:
        rows = self.db.execute(select(SysGroup).order_by(SysGroup.sort_code.asc())).scalars().all()
        return [SysGroupToGroupVO(row) for row in rows]


def get_group_service(db: Session = Depends(get_db)) -> GroupService:
    return GroupService.from_db(db)
