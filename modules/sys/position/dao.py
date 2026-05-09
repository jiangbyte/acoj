from typing import List, Optional
from sqlalchemy.orm import Session
from sqlalchemy import select, update, text
from .models import SysPosition
from core.db.base_dao import BaseDAO
from core.utils import generate_id
from datetime import datetime


class PositionDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysPosition)

    def insert(self, entity: SysPosition) -> SysPosition:
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

    def insert_batch(self, entities: List[SysPosition]) -> None:
        now = datetime.now()
        for entity in entities:
            entity.id = generate_id()
            if self._can_apply_soft_delete():
                setattr(entity, self._soft_delete_field, self._soft_delete_not_deleted)
            entity.created_at = now
            entity.updated_at = now
        self.db.add_all(entities)
        self.db.commit()

    def update(self, entity: SysPosition) -> SysPosition:
        entity.updated_at = datetime.now()
        self.db.commit()
        self.db.refresh(entity)
        return entity
