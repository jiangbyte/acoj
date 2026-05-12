from typing import List, Optional, Dict, Any
from sqlalchemy.orm import Session
from sqlalchemy import select, func
from .models import SysDict
from .params import DictPageParam, DictListParam
from core.db.base_dao import BaseDAO
from core.db.query_wrapper import QueryWrapper
from core.enums import SoftDeleteEnum


class DictDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysDict)

    def find_page_by_filters(self, param: DictPageParam) -> Dict[str, Any]:
        wrapper = QueryWrapper(SysDict)
        if param.parent_id:
            wrapper.where((SysDict.parent_id == param.parent_id) | (SysDict.id == param.parent_id))
        if param.category:
            wrapper.eq(SysDict.category, param.category)
        if param.keyword:
            wrapper.like(SysDict.label, param.keyword)
        wrapper.order_by_asc(SysDict.sort_code)
        return self.select_page(wrapper, param)

    def find_list_by_filters(self, param: DictListParam) -> List[SysDict]:
        wrapper = QueryWrapper(SysDict).order_by_asc(SysDict.sort_code)
        if param.parent_id is not None:
            wrapper.eq(SysDict.parent_id, param.parent_id)
        if param.category is not None:
            wrapper.eq(SysDict.category, param.category)
        return self.select_list(wrapper)

    def find_all_ordered(self) -> List[SysDict]:
        wrapper = QueryWrapper(SysDict).order_by_asc(SysDict.sort_code)
        return self.select_list(wrapper)

    def find_by_code(self, code: str) -> Optional[SysDict]:
        stmt = select(SysDict).where(
            SysDict.code == code,
            SysDict.is_deleted == SoftDeleteEnum.NO
        )
        return self.db.execute(stmt).scalar_one_or_none()

    def find_by_parent_id(self, parent_id: str) -> List[SysDict]:
        stmt = select(SysDict).where(
            SysDict.parent_id == parent_id,
            SysDict.is_deleted == SoftDeleteEnum.NO
        ).order_by(SysDict.sort_code)
        return list(self.db.execute(stmt).scalars().all())

    def has_children_batch(self, parent_ids: List[str]) -> set:
        if not parent_ids:
            return set()
        stmt = select(SysDict.parent_id).where(
            SysDict.parent_id.in_(parent_ids),
            SysDict.is_deleted == SoftDeleteEnum.NO
        ).distinct()
        return set(self.db.execute(stmt).scalars().all())

    def count_by_parent_and_label(self, parent_id: str, label: str, exclude_id: Optional[str] = None) -> int:
        stmt = select(func.count()).select_from(SysDict).where(
            SysDict.parent_id == parent_id,
            SysDict.label == label,
            SysDict.is_deleted == SoftDeleteEnum.NO
        )
        if exclude_id:
            stmt = stmt.where(SysDict.id != exclude_id)
        return self.db.execute(stmt).scalar() or 0

    def count_by_parent_and_value(self, parent_id: str, value: str, exclude_id: Optional[str] = None) -> int:
        stmt = select(func.count()).select_from(SysDict).where(
            SysDict.parent_id == parent_id,
            SysDict.value == value,
            SysDict.is_deleted == SoftDeleteEnum.NO
        )
        if exclude_id:
            stmt = stmt.where(SysDict.id != exclude_id)
        return self.db.execute(stmt).scalar() or 0
