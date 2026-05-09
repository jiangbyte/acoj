from typing import List, Optional
from sqlalchemy.orm import Session
from sqlalchemy import select
from .models import SysPermission
from core.db.base_dao import BaseDAO
from core.enums import SoftDeleteEnum
from core.utils import generate_id
from datetime import datetime


class PermissionDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysPermission)

    def insert(self, entity: SysPermission) -> SysPermission:
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

    def insert_batch(self, entities: List[SysPermission]) -> None:
        now = datetime.now()
        for entity in entities:
            entity.id = generate_id()
            if self._can_apply_soft_delete():
                setattr(entity, self._soft_delete_field, self._soft_delete_not_deleted)
            entity.created_at = now
            entity.updated_at = now
        self.db.add_all(entities)
        self.db.commit()

    def update(self, entity: SysPermission) -> SysPermission:
        entity.updated_at = datetime.now()
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def find_by_code(self, code: str) -> Optional[SysPermission]:
        return self.db.execute(
            select(SysPermission).where(SysPermission.code == code, SysPermission.is_deleted == SoftDeleteEnum.NO)
        ).scalar_one_or_none()

    def find_all_codes(self) -> List[str]:
        result = self.db.execute(
            select(SysPermission.code).where(SysPermission.is_deleted == SoftDeleteEnum.NO)
        ).scalars().all()
        return list(result)
