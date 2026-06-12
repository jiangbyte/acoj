"""Org service — explicit field-by-field matching Go pattern."""

from typing import Optional, List
from datetime import datetime
from fastapi import Request
from sqlalchemy.orm import Session
from sqlalchemy import update as sa_update, select, func
from .params import OrgVO, OrgPageParam, OrgTreeParam, SysOrgToOrgVO, SysOrgToOrgTreeVO
from .repository import OrgRepository
from .models import SysOrg
from ..user.models import SysUser
from ..group.models import SysGroup
from ..position.models import SysPosition
from sdk.web.result import page_data, PageDataField
from sdk.web.exception import BusinessException
from sdk.utils import generate_id
from sdk.auth import HeiAuthTool
def _sort_tree(nodes: List[dict]) -> None:
    nodes.sort(key=lambda x: x.get("sort_code", 0) or 0)
    for n in nodes:
        children = n.get("children")
        if children:
            _sort_tree(children)


def _check_circular_parent(db: Session, entity_id: str, new_parent_id: Optional[str]) -> None:
    if not new_parent_id:
        return
    all_rows = db.execute(select(SysOrg)).scalars().all()
    parent_map = {r.id: r.parent_id for r in all_rows}
    current = new_parent_id
    while current:
        if current == entity_id:
            raise BusinessException("父级不能选择自身或子节点")
        current = parent_map.get(current)
        if not current or current == "0":
            break


def _collect_descendant_ids(db: Session, ids: List[str]) -> List[str]:
    all_rows = db.execute(select(SysOrg)).scalars().all()
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

def page(db: Session, param: OrgPageParam) -> dict:
    repository = OrgRepository(db)
    result = repository.find_page_by_filters(param)
    records = [SysOrgToOrgVO(r) for r in result.get("records", [])]
    return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)


def detail(db: Session, id: str) -> Optional[dict]:
    if not id:
        return None
    entity = OrgRepository(db).find_by_id(id)
    if not entity:
        return None
    return SysOrgToOrgVO(entity)


def tree(db: Session, param: OrgTreeParam) -> list:
    all_rows = db.execute(select(SysOrg).order_by(SysOrg.sort_code.asc())).scalars().all()
    if param.category:
        all_rows = [r for r in all_rows if r.category == param.category]
    node_map = {}
    roots = []
    for r in all_rows:
        r_dict = SysOrgToOrgTreeVO(r).model_dump()
        node_map[r.id] = r_dict
    for r_dict in node_map.values():
        pid = r_dict.get("parent_id") or ""
        if pid and pid in node_map:
            node_map[pid]["children"].append(r_dict)
        else:
            roots.append(r_dict)
    _sort_tree(roots)
    return roots


def create(db: Session, vo: OrgVO, user_id: Optional[str] = None) -> None:
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
    if vo.parent_id is not None and vo.parent_id not in ("", "0"):
        entity.parent_id = vo.parent_id
    if vo.description is not None:
        entity.description = vo.description
    if vo.extra is not None:
        entity.extra = vo.extra
    if user_id:
        entity.created_by = user_id
        entity.updated_by = user_id
    OrgRepository(db).insert(entity)


def modify(db: Session, vo: OrgVO, user_id: Optional[str] = None) -> None:
    repository = OrgRepository(db)
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
        "sort_code": vo.sort_code,
        "updated_at": now,
    }
    if vo.parent_id is not None:
        up["parent_id"] = vo.parent_id if vo.parent_id not in ("", "0") else None
    else:
        up["parent_id"] = None
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
    repository.db.execute(sa_update(SysOrg).where(SysOrg.id == vo.id).values(**up))
    repository.db.commit()


def remove(db: Session, ids: list) -> None:
    if not ids:
        return
    all_ids = _collect_descendant_ids(db, ids)
    cnt_user = db.execute(select(func.count()).select_from(SysUser).where(SysUser.org_id.in_(all_ids))).scalar() or 0
    if cnt_user > 0:
        raise BusinessException("组织存在关联用户，无法删除")
    cnt_group = db.execute(select(func.count()).select_from(SysGroup).where(SysGroup.org_id.in_(all_ids))).scalar() or 0
    if cnt_group > 0:
        raise BusinessException("组织存在关联用户组，无法删除")
    cnt_pos = db.execute(select(func.count()).select_from(SysPosition).where(SysPosition.org_id.in_(all_ids))).scalar() or 0
    if cnt_pos > 0:
        raise BusinessException("组织存在关联职位，无法删除")
    OrgRepository(db).delete_by_ids(all_ids)


def options(db: Session) -> list:
    rows = db.execute(select(SysOrg).order_by(SysOrg.sort_code.asc())).scalars().all()
    return [SysOrgToOrgVO(r) for r in rows]


class OrgService:
    def __init__(self, db: Session):
        self.db = db
        self.repository = OrgRepository(db)

    async def _get_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            return await HeiAuthTool.getLoginIdDefaultNull(request)
        except Exception:
            return None

    def page(self, param: OrgPageParam) -> dict:
        return page(self.db, param)

    def detail(self, id: str):
        return detail(self.db, id)

    def tree(self, param: OrgTreeParam) -> list:
        return tree(self.db, param)

    async def create(self, vo: OrgVO, request: Optional[Request] = None) -> None:
        return create(self.db, vo, await self._get_user_id(request))

    async def modify(self, vo: OrgVO, request: Optional[Request] = None) -> None:
        return modify(self.db, vo, await self._get_user_id(request))

    def remove(self, ids: list) -> None:
        return remove(self.db, ids)

    def options(self) -> list:
        return options(self.db)
