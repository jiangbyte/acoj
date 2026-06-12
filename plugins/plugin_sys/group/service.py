"""Group service — explicit field-by-field matching Go pattern."""

from typing import Optional, List
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import update as sa_update, select, func
from fastapi import Request
from . import SysGroup
from .params import GroupVO, GroupPageParam, GroupTreeParam, SysGroupToGroupVO, SysGroupToGroupTreeVO
from .repository import GroupRepository
from ..org.params import OrgVO
from ..org.repository import OrgRepository
from ..org.models import SysOrg
from ..user.models import SysUser
from ..position.models import SysPosition
from core.result import page_data, PageDataField
from core.exception import BusinessException
from core.utils import generate_id
from core.auth import HeiAuthTool
from core.utils.resolve_utils import resolve_name_path
def _tree_node(entity: SysGroup) -> dict:
    return SysGroupToGroupTreeVO(entity).model_dump()


def _build_tree(node: dict, parent: SysGroup, children_map: dict) -> None:
    children = children_map.get(parent.id, [])
    if not children:
        return
    child_nodes = []
    for c in children:
        cn = _tree_node(c)
        _build_tree(cn, c, children_map)
        child_nodes.append(cn)
    node["children"] = child_nodes


def _sort_tree(nodes: List[dict]) -> None:
    nodes.sort(key=lambda x: x.get("sort_code", 0) or 0)
    for n in nodes:
        children = n.get("children")
        if children:
            _sort_tree(children)


def _enrich_vo(db: Session, vo: dict) -> None:
    vo["org_names"] = resolve_name_path(vo.get("org_id"), db, SysOrg)


def _batch_enrich(db: Session, vo_list: List[dict]) -> None:
    for vo in vo_list:
        _enrich_vo(db, vo)


def _check_circular_parent(db: Session, entity_id: str, new_parent_id: Optional[str]) -> None:
    if not new_parent_id:
        return
    all_rows = db.execute(select(SysGroup)).scalars().all()
    parent_map = {r.id: r.parent_id for r in all_rows}
    current = new_parent_id
    while current:
        if current == entity_id:
            raise BusinessException("父级不能选择自身或子节点")
        current = parent_map.get(current)
        if not current or current == "0":
            break


def _collect_descendant_ids(db: Session, ids: List[str]) -> List[str]:
    all_rows = db.execute(select(SysGroup)).scalars().all()
    children_map = {}
    for r in all_rows:
        pid = r.parent_id or ""
        children_map.setdefault(pid, []).append(r.id)
    all_ids = set(ids)
    stack = list(ids)
    while stack:
        pid = stack.pop()
        for cid in children_map.get(pid, []):
            if cid not in all_ids:
                all_ids.add(cid)
                stack.append(cid)
    return list(all_ids)


# ── Service functions ──

def page(db: Session, param: GroupPageParam) -> dict:
    repository = GroupRepository(db)
    result = repository.find_page_by_filters(param)
    records = [SysGroupToGroupVO(r) for r in result.get("records", [])]
    _batch_enrich(db, records)
    return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)


def detail(db: Session, id: str) -> Optional[dict]:
    if not id:
        return None
    entity = GroupRepository(db).find_by_id(id)
    if not entity:
        return None
    vo = SysGroupToGroupVO(entity)
    _enrich_vo(db, vo)
    return vo


def tree(db: Session, param: GroupTreeParam) -> list:
    all_rows = db.execute(select(SysGroup).order_by(SysGroup.sort_code.asc())).scalars().all()
    filtered = all_rows
    if param.category:
        filtered = [r for r in filtered if r.category == param.category]

    children_map = {}
    for r in filtered:
        pid = r.parent_id or ""
        children_map.setdefault(pid, []).append(r)

    roots = children_map.get("", [])
    result = []
    for r in roots:
        node = _tree_node(r)
        _build_tree(node, r, children_map)
        result.append(node)
    _sort_tree(result)
    return result


def create(db: Session, vo: GroupVO, user_id: Optional[str] = None) -> None:
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
    if user_id:
        entity.created_by = user_id
        entity.updated_by = user_id
    GroupRepository(db).insert(entity)


def modify(db: Session, vo: GroupVO, user_id: Optional[str] = None) -> None:
    repository = GroupRepository(db)
    entity = repository.find_by_id(vo.id)
    if not entity:
        raise BusinessException("数据不存在")
    if vo.parent_id is not None and vo.parent_id != entity.parent_id:
        _check_circular_parent(db, vo.id, vo.parent_id)
    now = datetime.now()
    up = {
        "code": vo.code,
        "name": vo.name,
        "category": vo.category,
        "org_id": vo.org_id or "",
        "parent_id": vo.parent_id,
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
    repository.db.execute(sa_update(SysGroup).where(SysGroup.id == vo.id).values(**up))
    repository.db.commit()


def remove(db: Session, ids: list) -> None:
    if not ids:
        return
    all_ids = _collect_descendant_ids(db, ids)
    db.execute(sa_update(SysUser).where(SysUser.group_id.in_(all_ids)).values(group_id=None))
    GroupRepository(db).delete_by_ids(all_ids)


def options(db: Session) -> list:
    rows = db.execute(select(SysGroup).order_by(SysGroup.sort_code.asc())).scalars().all()
    return [SysGroupToGroupVO(r) for r in rows]


def union_tree(db: Session) -> list:
    org_rows = db.execute(select(SysOrg).order_by(SysOrg.sort_code.asc())).scalars().all()
    group_rows = db.execute(select(SysGroup).order_by(SysGroup.sort_code.asc())).scalars().all()

    org_nodes = {}
    for o in org_rows:
        node = OrgVO.model_validate(o).model_dump()
        node["_type"] = "org"
        node["children"] = []
        org_nodes[o.id] = node

    group_nodes = {}
    for g in group_rows:
        node = SysGroupToGroupVO(g)
        node["_type"] = "group"
        node["children"] = []
        group_nodes[g.id] = node

    for gid, node in group_nodes.items():
        pid = node.get("parent_id") or ""
        if pid and pid in group_nodes:
            group_nodes[pid]["children"].append(node)

    orphan_groups = {}
    for gid, node in group_nodes.items():
        pid = node.get("parent_id") or ""
        if not pid or pid not in group_nodes:
            oid = node.get("org_id") or ""
            orphan_groups.setdefault(oid, []).append(node)

    for oid, node in org_nodes.items():
        if oid in orphan_groups:
            node["children"] = orphan_groups[oid] + node["children"]

    roots = []
    for oid, node in org_nodes.items():
        pid = node.get("parent_id") or ""
        if pid and pid in org_nodes:
            org_nodes[pid]["children"].append(node)
        else:
            roots.append(node)

    _sort_tree(roots)
    return roots


class GroupService:
    def __init__(self, db: Session):
        self.db = db
        self.repository = GroupRepository(db)

    async def _get_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            return await HeiAuthTool.getLoginIdDefaultNull(request)
        except Exception:
            return None

    def page(self, param: GroupPageParam) -> dict:
        return page(self.db, param)

    def detail(self, id: str):
        return detail(self.db, id)

    def tree(self, param: GroupTreeParam) -> list:
        return tree(self.db, param)

    def union_tree(self) -> list:
        return union_tree(self.db)

    async def create(self, vo: GroupVO, request: Optional[Request] = None) -> None:
        return create(self.db, vo, await self._get_user_id(request))

    async def modify(self, vo: GroupVO, request: Optional[Request] = None) -> None:
        return modify(self.db, vo, await self._get_user_id(request))

    def remove(self, ids: list) -> None:
        return remove(self.db, ids)

    def options(self) -> list:
        return options(self.db)

def get_all(db: Session) -> list:
    return options(db)
