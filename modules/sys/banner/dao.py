from typing import Optional, List
from sqlalchemy.orm import Session
from sqlalchemy import select
from .models import SysBanner
from core.db.base_dao import BaseDAO
from core.utils import generate_id
from datetime import datetime, timezone


class BannerDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysBanner)

    def find_by_code(self, code: str) -> Optional[SysBanner]:
        query = select(SysBanner).where(SysBanner.code == code)
        query = self._apply_soft_delete_filter(query)
        return self.db.execute(query).scalar_one_or_none()

    def insert(self, entity: SysBanner) -> SysBanner:
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

    def insert_batch(self, entities: List[SysBanner]) -> None:
        now = datetime.now()
        for entity in entities:
            entity.id = generate_id()
            if self._can_apply_soft_delete():
                setattr(entity, self._soft_delete_field, self._soft_delete_not_deleted)
            entity.created_at = now
            entity.updated_at = now
        self.db.add_all(entities)
        self.db.commit()

    def update(self, entity: SysBanner) -> SysBanner:
        entity.updated_at = datetime.now()
        self.db.commit()
        self.db.refresh(entity)
        return entity
