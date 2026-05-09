from typing import List, Optional
from sqlalchemy.orm import Session
from sqlalchemy import select, func
from .models import SysDict
from core.db.base_dao import BaseDAO
from core.enums import SoftDeleteEnum
from core.utils import generate_id
from datetime import datetime


class DictDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysDict)

    def insert(self, entity: SysDict) -> SysDict:
        entity.id = generate_id()
        if self._can_apply_soft_delete():
            setattr(entity, self._soft_delete_field, self._soft_delete_not_deleted)
        now = datetime.now()
        entity.created_at = now
        entity.updated_at = now
        self.db.add(entity)
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def insert_batch(self, entities: List[SysDict]) -> None:
        now = datetime.now()
        for entity in entities:
            entity.id = generate_id()
            if self._can_apply_soft_delete():
                setattr(entity, self._soft_delete_field, self._soft_delete_not_deleted)
            entity.created_at = now
            entity.updated_at = now
        self.db.add_all(entities)
        self.db.commit()

    def update(self, entity: SysDict) -> SysDict:
        entity.updated_at = datetime.now()
        self.db.commit()
        self.db.refresh(entity)
        return entity

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
