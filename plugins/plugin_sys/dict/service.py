"""
Dict service — explicit field-by-field matching Go's dict/service.go pattern.
"""

from typing import Optional, List
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import update as sa_update, select, func
from fastapi import Request
from .params import DictVO, DictPageParam, DictListParam, DictTreeParam
from .dao import DictDao
from .models import SysDict
from core.result import page_data, PageDataField
from core.exception import BusinessException
from core.utils import generate_id
from core.auth import HeiAuthTool
from core.db.redis import get_client
from core.constants import DICT_CACHE_KEY, DICT_TREE_CACHE_KEY
import json
import logging

logger = logging.getLogger(__name__)


def _safe_str(s: Optional[str]) -> str:
    return s or ""


def _get_parent_id_key(parent_id: Optional[str]) -> str:
    if not parent_id or parent_id == "0":
        return ""
    return parent_id


def to_vo(entity: SysDict) -> dict:
    vo = {
        "id": entity.id,
        "code": entity.code,
        "status": entity.status,
        "sort_code": entity.sort_code,
    }
    if entity.label is not None:
        vo["label"] = entity.label
    if entity.value is not None:
        vo["value"] = entity.value
    if entity.color is not None:
        vo["color"] = entity.color
    if entity.category is not None:
        vo["category"] = entity.category
    if entity.parent_id is not None:
        vo["parent_id"] = entity.parent_id
    if entity.created_at is not None:
        vo["created_at"] = entity.created_at.strftime("%Y-%m-%d %H:%M:%S")
    if entity.created_by is not None:
        vo["created_by"] = entity.created_by
    if entity.updated_at is not None:
        vo["updated_at"] = entity.updated_at.strftime("%Y-%m-%d %H:%M:%S")
    if entity.updated_by is not None:
        vo["updated_by"] = entity.updated_by
    return vo


def entity_to_node(e: SysDict) -> dict:
    node = {"id": e.id, "code": e.code, "status": e.status, "sort_code": e.sort_code}
    if e.label is not None:
        node["label"] = e.label
    if e.value is not None:
        node["value"] = e.value
    if e.color is not None:
        node["color"] = e.color
    if e.category is not None:
        node["category"] = e.category
    if e.parent_id is not None:
        node["parent_id"] = e.parent_id
    return node


def _sort_tree(nodes: List[dict]) -> None:
    nodes.sort(key=lambda x: x.get("sort_code", 0) or 0)
    for n in nodes:
        children = n.get("children")
        if children:
            _sort_tree(children)


def _build_tree_children(children_map: dict, parent_id: str, depth: int = 0) -> List[dict]:
    if depth > 50:
        return []
    children = children_map.get(parent_id, [])
    if not children:
        return []
    result = []
    for r in children:
        node = entity_to_node(r)
        node["children"] = _build_tree_children(children_map, r.id, depth + 1)
        result.append(node)
    _sort_tree(result)
    return result


def _check_duplicate(ctx_db: Session, vo: DictVO, exclude_id: Optional[str] = None) -> None:
    """Check if a sibling dict with the same value already exists."""
    if vo.value:
        q = select(func.count()).select_from(SysDict).where(
            SysDict.parent_id == vo.parent_id,
            SysDict.value == vo.value,
        )
        if exclude_id:
            q = q.where(SysDict.id != exclude_id)
        cnt = ctx_db.execute(q).scalar() or 0
        if cnt > 0:
            raise BusinessException(f"同一父字典下已存在相同值 {vo.value}", 400)


def _check_circular_parent(ctx_db: Session, entity_id: str, new_parent_id: str) -> None:
    if not new_parent_id or new_parent_id in ("", "0") or not entity_id:
        return
    all_rows = ctx_db.execute(select(SysDict)).scalars().all()
    parent_map = {}
    for e in all_rows:
        if e.parent_id:
            parent_map[e.id] = e.parent_id
    current = new_parent_id
    while current:
        if current == entity_id:
            raise BusinessException("父级不能选择自身或子节点", 400)
        current = parent_map.get(current, "")


def _collect_descendant_ids(ctx_db: Session, ids: List[str]) -> List[str]:
    all_rows = ctx_db.execute(select(SysDict)).scalars().all()
    children_map = {}
    for r in all_rows:
        pid = _get_parent_id_key(r.parent_id)
        children_map.setdefault(pid, []).append(r.id)
    all_ids = set(ids)
    stack = list(ids)
    while stack:
        pid = stack.pop()
        for child_id in children_map.get(pid, []):
            if child_id not in all_ids:
                all_ids.add(child_id)
                stack.append(child_id)
    return list(all_ids)


# ── Service functions ──

def page(db: Session, param: DictPageParam) -> dict:
    dao = DictDao(db)
    result = dao.find_page_by_filters(param)
    records = [to_vo(r) for r in result.get("records", [])]
    return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)


def list_dicts(db: Session, param: DictListParam) -> list:
    records = DictDao(db).find_list_by_filters(param)
    return [to_vo(r) for r in records]


def tree(db: Session, param: DictTreeParam) -> list:
    q = select(SysDict).order_by(SysDict.sort_code.asc())
    if param.category:
        q = q.where(SysDict.category == param.category)
    if param.dict_group == "FRM":
        q = q.where(SysDict.category == "FRM")
    if param.dict_group == "BIZ":
        q = q.where(SysDict.category == "BIZ")
    all_rows = db.execute(q).scalars().all()
    if not all_rows:
        return []
    children_map = {}
    for r in all_rows:
        pid = _get_parent_id_key(r.parent_id)
        children_map.setdefault(pid, []).append(r)
    roots = children_map.get("", [])
    result = []
    for r in roots:
        node = entity_to_node(r)
        node["children"] = _build_tree_children(children_map, r.id, 0)
        result.append(node)
    _sort_tree(result)
    return result


def detail(db: Session, id: str) -> Optional[dict]:
    if not id:
        return None
    entity = DictDao(db).find_by_id(id)
    if not entity:
        return None
    return to_vo(entity)


def create(db: Session, vo: DictVO, user_id: Optional[str] = None) -> None:
    _check_duplicate(db, vo, None)

    now = datetime.now()
    entity = SysDict(
        id=generate_id(),
        code=vo.code,
        status="ENABLED",
        sort_code=vo.sort_code or 0,
        created_at=now,
        updated_at=now,
    )
    if vo.label is not None:
        entity.label = vo.label
    if vo.value is not None:
        entity.value = vo.value
    if vo.color is not None:
        entity.color = vo.color
    if vo.category is not None:
        entity.category = vo.category
    if vo.parent_id is not None and vo.parent_id not in ("", "0"):
        entity.parent_id = vo.parent_id
    if user_id:
        entity.created_by = user_id
        entity.updated_by = user_id
    DictDao(db).insert(entity)


def modify(db: Session, vo: DictVO, user_id: Optional[str] = None) -> None:
    dao = DictDao(db)
    entity = dao.find_by_id(vo.id)
    if not entity:
        raise BusinessException("数据不存在", 400)

    _check_duplicate(db, vo, vo.id)

    if vo.parent_id is not None and vo.parent_id not in ("", "0") and vo.parent_id != _get_parent_id_key(entity.parent_id):
        _check_circular_parent(db, vo.id, vo.parent_id)

    now = datetime.now()
    up = {
        "code": vo.code,
        "sort_code": vo.sort_code,
        "updated_at": now,
    }
    if vo.label is not None:
        up["label"] = vo.label
    if vo.value is not None:
        up["value"] = vo.value
    if vo.color is not None:
        up["color"] = vo.color
    if vo.category is not None:
        up["category"] = vo.category
    if vo.parent_id is not None:
        up["parent_id"] = vo.parent_id if vo.parent_id not in ("", "0") else None
    if user_id:
        up["updated_by"] = user_id
    dao.db.execute(sa_update(SysDict).where(SysDict.id == vo.id).values(**up))
    dao.db.commit()


def remove(db: Session, ids: list) -> None:
    if not ids:
        return
    all_ids = _collect_descendant_ids(db, ids)
    DictDao(db).delete_by_ids(all_ids)


def options(db: Session) -> list:
    rows = db.execute(select(SysDict).order_by(SysDict.sort_code.asc())).scalars().all()
    return [to_vo(r) for r in rows]


def get_dict_label(db: Session, type_code: str, value: str) -> Optional[str]:
    """Get dict label by type code and value (subquery match Go's DictGetLabel)."""
    entity = db.execute(
        select(SysDict).where(
            SysDict.parent_id.in_(select(SysDict.id).where(SysDict.code == type_code)),
            SysDict.value == value,
        )
    ).scalar_one_or_none()
    if not entity:
        return None
    return entity.label


def get_dict_children(db: Session, type_code: str) -> list:
    dao = DictDao(db)
    root = dao.find_by_code(type_code)
    if not root:
        return []
    children = dao.find_by_parent_id(root.id)
    return [to_vo(c) for c in children]


# ── Backward-compat class ──

class DictService:
    def __init__(self, db: Session):
        self.db = db
        self.dao = DictDao(db)

    async def _get_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            return await HeiAuthTool.getLoginIdDefaultNull(request)
        except Exception:
            return None

    def page(self, param: DictPageParam) -> dict:
        return page(self.db, param)

    def list(self, param: DictListParam) -> list:
        return list_dicts(self.db, param)

    def tree(self, param: DictTreeParam) -> list:
        return tree(self.db, param)

    def detail(self, id: str):
        return detail(self.db, id)

    async def create(self, vo: DictVO, request: Optional[Request] = None) -> None:
        return create(self.db, vo, await self._get_user_id(request))

    async def modify(self, vo: DictVO, request: Optional[Request] = None) -> None:
        return modify(self.db, vo, await self._get_user_id(request))

    def remove(self, ids: list) -> None:
        return remove(self.db, ids)

    def get_dict_label(self, type_code: str, value: str) -> Optional[str]:
        return get_dict_label(self.db, type_code, value)

    def get_dict_children(self, type_code: str) -> list:
        return get_dict_children(self.db, type_code)
