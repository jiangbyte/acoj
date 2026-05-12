from typing import List
from datetime import datetime
from sqlalchemy.orm import Session
from .models import SysGroup
from core.db.base_dao import BaseDAO
from core.utils import generate_id


class GroupDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysGroup)

    def insert(self, entity: SysGroup) -> SysGroup:
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

    def insert_batch(self, entities: List[SysGroup]) -> None:
        now = datetime.now()
        for entity in entities:
            entity.id = generate_id()
            if self._can_apply_soft_delete():
                setattr(entity, self._soft_delete_field, self._soft_delete_not_deleted)
            entity.created_at = now
            entity.updated_at = now
        self.db.add_all(entities)
        self.db.commit()

    def update(self, entity: SysGroup) -> SysGroup:
        entity.updated_at = datetime.now()
        self.db.commit()
        self.db.refresh(entity)
        return entity
