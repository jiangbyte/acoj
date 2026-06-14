"""Dict service."""

from datetime import datetime
from typing import List, Optional

from fastapi import Depends
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from sdk.utils import generate_id
from sdk.utils.tree_utils import build_tree, collect_descendant_ids
from sdk.web.exception import BusinessException
from sdk.web.result import map_page_data

from .models import SysDict
from .params import (
    DictListParam,
    DictPageParam,
    DictTreeParam,
    DictVO,
    DictTreeVO,
)
from .repository import DictRepository


def _normalize_parent_id(parent_id: Optional[str]) -> Optional[str]:
    return parent_id if parent_id not in (None, "", "0") else None


def _parent_key(parent_id: Optional[str]) -> str:
    return _normalize_parent_id(parent_id) or ""


def _actor_user_id(actor: Optional[ActorContext]) -> Optional[str]:
    return actor.user_id if actor else None


def _entity_to_node(entity: SysDict) -> DictTreeVO:
    return DictTreeVO.model_validate(entity)


class DictService:
    def __init__(self, repository: DictRepository):
        self.repository = repository
        self.db = repository.db

    async def _check_duplicate(self, vo: DictVO, exclude_id: Optional[str] = None) -> None:
        if not vo.value:
            return
        query = select(func.count()).select_from(SysDict).where(
            SysDict.parent_id == _normalize_parent_id(vo.parent_id),
            SysDict.value == vo.value,
        )
        if exclude_id:
            query = query.where(SysDict.id != exclude_id)
        count = (await self.db.execute(query)).scalar() or 0
        if count > 0:
            raise BusinessException(f"同一父字典下已存在相同值 {vo.value}", 400)

    async def _check_circular_parent(self, entity_id: str, new_parent_id: str) -> None:
        normalized_parent_id = _normalize_parent_id(new_parent_id)
        if not normalized_parent_id or not entity_id:
            return
        all_rows = (await self.db.execute(select(SysDict))).scalars().all()
        parent_map = {row.id: row.parent_id for row in all_rows if row.parent_id}
        current = normalized_parent_id
        while current:
            if current == entity_id:
                raise BusinessException("父级不能选择自身或子节点", 400)
            current = parent_map.get(current, "")

    async def _collect_descendant_ids(self, ids: List[str]) -> List[str]:
        all_rows = (await self.db.execute(select(SysDict))).scalars().all()
        return collect_descendant_ids(
            all_rows,
            ids,
            get_id=lambda row: row.id,
            get_parent_id=lambda row: _parent_key(row.parent_id),
        )

    async def page(self, param: DictPageParam) -> dict:
        return map_page_data(
            await self.repository.find_page_by_filters(param),
            DictVO.model_validate,
            param.current,
            param.size,
        )

    async def list(self, param: DictListParam) -> list:
        rows = await self.repository.find_list_by_filters(param)
        return [DictVO.model_validate(row) for row in rows]

    async def tree(self, param: DictTreeParam) -> List[DictTreeVO]:
        query = select(SysDict).order_by(SysDict.sort_code.asc())
        if param.category:
            query = query.where(SysDict.category == param.category)
        if param.dict_group == "FRM":
            query = query.where(SysDict.category == "FRM")
        if param.dict_group == "BIZ":
            query = query.where(SysDict.category == "BIZ")

        rows = (await self.db.execute(query)).scalars().all()
        if not rows:
            return []
        nodes = [_entity_to_node(row) for row in rows]
        return build_tree(
            nodes,
            get_id=lambda node: node.id or "",
            get_parent_id=lambda node: _parent_key(node.parent_id),
            get_children=lambda node: node.children,
            get_sort_code=lambda node: node.sort_code,
        )

    async def detail(self, id: str) -> Optional[DictVO]:
        if not id:
            return None
        entity = await self.repository.find_by_id(id)
        if not entity:
            return None
        return DictVO.model_validate(entity)

    async def create(self, vo: DictVO, actor: Optional[ActorContext] = None) -> None:
        await self._check_duplicate(vo)
        now = datetime.now()
        actor_user_id = _actor_user_id(actor)
        entity = SysDict(
            id=generate_id(),
            code=vo.code,
            label=vo.label,
            value=vo.value,
            color=vo.color,
            category=vo.category,
            parent_id=_normalize_parent_id(vo.parent_id),
            status="ENABLED",
            sort_code=vo.sort_code or 0,
            created_at=now,
            updated_at=now,
            created_by=actor_user_id,
            updated_by=actor_user_id,
        )
        await self.repository.insert(entity)

    async def modify(self, vo: DictVO, actor: Optional[ActorContext] = None) -> None:
        entity = await self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在", 400)

        await self._check_duplicate(vo, vo.id)
        if (
            vo.parent_id is not None
            and _normalize_parent_id(vo.parent_id) != _normalize_parent_id(entity.parent_id)
        ):
            await self._check_circular_parent(vo.id, vo.parent_id)

        actor_user_id = _actor_user_id(actor)
        updates = {
            "code": vo.code,
            "label": vo.label,
            "value": vo.value,
            "color": vo.color,
            "category": vo.category,
            "parent_id": _normalize_parent_id(vo.parent_id),
            "sort_code": vo.sort_code,
            "updated_at": datetime.now(),
        }
        if actor_user_id:
            updates["updated_by"] = actor_user_id
        await self.repository.update_by_id(vo.id, updates)

    async def remove(self, ids: list) -> None:
        if not ids:
            return
        await self.repository.delete_by_ids(await self._collect_descendant_ids(ids))

    async def get_dict_label(self, type_code: str, value: str) -> Optional[str]:
        entity = (await self.db.execute(
            select(SysDict).where(
                SysDict.parent_id.in_(select(SysDict.id).where(SysDict.code == type_code)),
                SysDict.value == value,
            )
        )).scalar_one_or_none()
        if not entity:
            return None
        return entity.label

    async def get_dict_children(self, type_code: str) -> list:
        root = await self.repository.find_by_code(type_code)
        if not root:
            return []
        return [DictVO.model_validate(row) for row in await self.repository.find_by_parent_id(root.id)]


def get_dict_service(db: AsyncSession = Depends(get_db)) -> DictService:
    return DictService(DictRepository(db))
