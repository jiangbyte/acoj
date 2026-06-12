"""Dict service."""

from datetime import datetime
from typing import List, Optional

from fastapi import Depends
from sqlalchemy import func, select
from sqlalchemy.orm import Session

from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from sdk.utils import generate_id
from sdk.web.exception import BusinessException
from sdk.web.result import PageDataField, page_data

from .models import SysDict
from .params import (
    DictListParam,
    DictPageParam,
    DictTreeParam,
    DictVO,
    SysDictToDictTreeVO,
    SysDictToDictVO,
)
from .repository import DictRepository


def _get_parent_id_key(parent_id: Optional[str]) -> str:
    if not parent_id or parent_id == "0":
        return ""
    return parent_id


def _entity_to_node(entity: SysDict) -> dict:
    return SysDictToDictTreeVO(entity).model_dump()


def _sort_tree(nodes: List[dict]) -> None:
    nodes.sort(key=lambda item: item.get("sort_code", 0) or 0)
    for node in nodes:
        children = node.get("children")
        if children:
            _sort_tree(children)


def _build_tree_children(children_map: dict, parent_id: str, depth: int = 0) -> List[dict]:
    if depth > 50:
        return []
    children = children_map.get(parent_id, [])
    if not children:
        return []

    result = []
    for row in children:
        node = _entity_to_node(row)
        node["children"] = _build_tree_children(children_map, row.id, depth + 1)
        result.append(node)
    _sort_tree(result)
    return result


class DictService:
    def __init__(self, repository: DictRepository):
        self.repository = repository
        self.db = repository.db

    @classmethod
    def from_db(cls, db: Session) -> "DictService":
        return cls(DictRepository(db))

    def _check_duplicate(self, vo: DictVO, exclude_id: Optional[str] = None) -> None:
        if not vo.value:
            return
        query = select(func.count()).select_from(SysDict).where(
            SysDict.parent_id == vo.parent_id,
            SysDict.value == vo.value,
        )
        if exclude_id:
            query = query.where(SysDict.id != exclude_id)
        count = self.db.execute(query).scalar() or 0
        if count > 0:
            raise BusinessException(f"同一父字典下已存在相同值 {vo.value}", 400)

    def _check_circular_parent(self, entity_id: str, new_parent_id: str) -> None:
        if not new_parent_id or new_parent_id in ("", "0") or not entity_id:
            return
        all_rows = self.db.execute(select(SysDict)).scalars().all()
        parent_map = {row.id: row.parent_id for row in all_rows if row.parent_id}
        current = new_parent_id
        while current:
            if current == entity_id:
                raise BusinessException("父级不能选择自身或子节点", 400)
            current = parent_map.get(current, "")

    def _collect_descendant_ids(self, ids: List[str]) -> List[str]:
        all_rows = self.db.execute(select(SysDict)).scalars().all()
        children_map: dict[str, list[str]] = {}
        for row in all_rows:
            children_map.setdefault(_get_parent_id_key(row.parent_id), []).append(row.id)
        all_ids = set(ids)
        stack = list(ids)
        while stack:
            parent_id = stack.pop()
            for child_id in children_map.get(parent_id, []):
                if child_id not in all_ids:
                    all_ids.add(child_id)
                    stack.append(child_id)
        return list(all_ids)

    def page(self, param: DictPageParam) -> dict:
        result = self.repository.find_page_by_filters(param)
        records = [SysDictToDictVO(row) for row in result.get("records", [])]
        return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)

    def list(self, param: DictListParam) -> list:
        rows = self.repository.find_list_by_filters(param)
        return [SysDictToDictVO(row) for row in rows]

    def tree(self, param: DictTreeParam) -> list:
        query = select(SysDict).order_by(SysDict.sort_code.asc())
        if param.category:
            query = query.where(SysDict.category == param.category)
        if param.dict_group == "FRM":
            query = query.where(SysDict.category == "FRM")
        if param.dict_group == "BIZ":
            query = query.where(SysDict.category == "BIZ")

        rows = self.db.execute(query).scalars().all()
        if not rows:
            return []

        children_map: dict[str, list[SysDict]] = {}
        for row in rows:
            children_map.setdefault(_get_parent_id_key(row.parent_id), []).append(row)

        result = []
        for row in children_map.get("", []):
            node = _entity_to_node(row)
            node["children"] = _build_tree_children(children_map, row.id, 0)
            result.append(node)
        _sort_tree(result)
        return result

    def detail(self, id: str) -> Optional[dict]:
        if not id:
            return None
        entity = self.repository.find_by_id(id)
        if not entity:
            return None
        return SysDictToDictVO(entity)

    def create(self, vo: DictVO, actor: Optional[ActorContext] = None) -> None:
        self._check_duplicate(vo)
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

        self._check_duplicate(vo, vo.id)
        if (
            vo.parent_id is not None
            and vo.parent_id not in ("", "0")
            and vo.parent_id != _get_parent_id_key(entity.parent_id)
        ):
            self._check_circular_parent(vo.id, vo.parent_id)

        updates = {
            "code": vo.code,
            "sort_code": vo.sort_code,
            "updated_at": datetime.now(),
        }
        if vo.label is not None:
            updates["label"] = vo.label
        if vo.value is not None:
            updates["value"] = vo.value
        if vo.color is not None:
            updates["color"] = vo.color
        if vo.category is not None:
            updates["category"] = vo.category
        if vo.parent_id is not None:
            updates["parent_id"] = vo.parent_id if vo.parent_id not in ("", "0") else None
        if actor and actor.user_id:
            updates["updated_by"] = actor.user_id
        self.repository.update_by_id(vo.id, updates)

    def remove(self, ids: list) -> None:
        if not ids:
            return
        self.repository.delete_by_ids(self._collect_descendant_ids(ids))

    def get_dict_label(self, type_code: str, value: str) -> Optional[str]:
        entity = self.db.execute(
            select(SysDict).where(
                SysDict.parent_id.in_(select(SysDict.id).where(SysDict.code == type_code)),
                SysDict.value == value,
            )
        ).scalar_one_or_none()
        return entity.label if entity else None

    def get_dict_children(self, type_code: str) -> list:
        root = self.repository.find_by_code(type_code)
        if not root:
            return []
        return [SysDictToDictVO(row) for row in self.repository.find_by_parent_id(root.id)]


def get_dict_service(db: Session = Depends(get_db)) -> DictService:
    return DictService.from_db(db)
