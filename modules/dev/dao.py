from typing import Optional, List
from sqlalchemy.orm import Session
from sqlalchemy import select
from .models import GenBasic, GenConfig
from core.db.base_dao import BaseDAO
from core.utils import generate_id
from datetime import datetime


class GenBasicDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, GenBasic)

    def insert(self, entity: GenBasic) -> GenBasic:
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

    def insert_batch(self, entities: List[GenBasic]) -> None:
        now = datetime.now()
        for entity in entities:
            entity.id = generate_id()
            if self._can_apply_soft_delete():
                setattr(entity, self._soft_delete_field, self._soft_delete_not_deleted)
            entity.created_at = now
            entity.updated_at = now
        self.db.add_all(entities)
        self.db.commit()

    def update(self, entity: GenBasic) -> GenBasic:
        entity.updated_at = datetime.now()
        self.db.commit()
        self.db.refresh(entity)
        return entity


class GenConfigDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, GenConfig)

    def find_by_basic_id(self, basic_id: str, table_type: Optional[str] = None) -> List[GenConfig]:
        query = select(GenConfig).where(GenConfig.basic_id == basic_id)
        query = self._apply_soft_delete_filter(query)
        if table_type:
            query = query.where(GenConfig.table_type == table_type)
        query = query.order_by(GenConfig.sort_code)
        return list(self.db.execute(query).scalars().all())

    def delete_by_basic_id(self, basic_id: str) -> int:
        entities = self.find_by_basic_id(basic_id)
        if not entities:
            return 0
        if self._can_apply_soft_delete():
            for entity in entities:
                setattr(entity, self._soft_delete_field, self._soft_delete_deleted)
            self.db.commit()
        else:
            for entity in entities:
                self.db.delete(entity)
            self.db.commit()
        return len(entities)

    def insert(self, entity: GenConfig) -> GenConfig:
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

    def insert_batch(self, entities: List[GenConfig]) -> None:
        now = datetime.now()
        for entity in entities:
            entity.id = generate_id()
            if self._can_apply_soft_delete():
                setattr(entity, self._soft_delete_field, self._soft_delete_not_deleted)
            entity.created_at = now
            entity.updated_at = now
        self.db.add_all(entities)
        self.db.commit()

    def update(self, entity: GenConfig) -> GenConfig:
        entity.updated_at = datetime.now()
        self.db.commit()
        self.db.refresh(entity)
        return entity
