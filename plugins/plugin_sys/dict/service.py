"""Dict service — class-based service with DI-friendly provider."""

from typing import Optional, List
from datetime import datetime
from fastapi import Depends
from sqlalchemy import func, select
from sqlalchemy.orm import Session

from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from .params import DictVO, DictPageParam, DictListParam, DictTreeParam, SysDictToDictVO, SysDictToDictTreeVO
from .repository import DictRepository
from .models import SysDict
from sdk.web.result import page_data, PageDataField
from sdk.web.exception import BusinessException
from sdk.utils import generate_id
import logging

logger = logging.getLogger(__name__)


def _safe_str(s: Optional[str]) -> str:
    return s or ""


def _get_parent_id_key(parent_id: Optional[str]) -> str:
    if not parent_id or parent_id == "0":
        return ""
    return parent_id
def entity_to_node(e: SysDict) -> dict:
    return SysDictToDictTreeVO(e).model_dump()


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
    repository = DictRepository(db)
    result = repository.find_page_by_filters(param)
    records = [SysDictToDictVO(r) for r in result.get("records", [])]
    return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)


def list_dicts(db: Session, param: DictListParam) -> list:
    records = DictRepository(db).find_list_by_filters(param)
    return [SysDictToDictVO(r) for r in records]


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
    entity = DictRepository(db).find_by_id(id)
    if not entity:
        return None
    return SysDictToDictVO(entity)


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
    repository = DictRepository(db)
    root = repository.find_by_code(type_code)
    if not root:
        return []
    children = repository.find_by_parent_id(root.id)
    return [SysDictToDictVO(c) for c in children]


# ── Backward-compat class ──

class DictService:
    def __init__(self, repository: DictRepository):
        self.repository = repository
        self.db = repository.db

    @classmethod
    def from_db(cls, db: Session) -> "DictService":
        return cls(DictRepository(db))

    def page(self, param: DictPageParam) -> dict:
        return page(self.db, param)

    def list(self, param: DictListParam) -> list:
        return list_dicts(self.db, param)

    def tree(self, param: DictTreeParam) -> list:
        return tree(self.db, param)

    def detail(self, id: str):
        return detail(self.db, id)

    def create(self, vo: DictVO, actor: Optional[ActorContext] = None) -> None:
        _check_duplicate(self.db, vo, None)
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
        if actor and actor.user_id:
            entity.created_by = actor.user_id
            entity.updated_by = actor.user_id
        self.repository.insert(entity)

    def modify(self, vo: DictVO, actor: Optional[ActorContext] = None) -> None:
        entity = self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在", 400)
        _check_duplicate(self.db, vo, vo.id)
        if vo.parent_id is not None and vo.parent_id not in ("", "0") and vo.parent_id != _get_parent_id_key(entity.parent_id):
            _check_circular_parent(self.db, vo.id, vo.parent_id)
        up = {
            "code": vo.code,
            "sort_code": vo.sort_code,
            "updated_at": datetime.now(),
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
        if actor and actor.user_id:
            up["updated_by"] = actor.user_id
        self.repository.update_by_id(vo.id, up)

    def remove(self, ids: list) -> None:
        if not ids:
            return
        all_ids = _collect_descendant_ids(self.db, ids)
        self.repository.delete_by_ids(all_ids)

    def get_dict_label(self, type_code: str, value: str) -> Optional[str]:
        return get_dict_label(self.db, type_code, value)

    def get_dict_children(self, type_code: str) -> list:
        return get_dict_children(self.db, type_code)


def get_dict_service(db: Session = Depends(get_db)) -> DictService:
    return DictService.from_db(db)
