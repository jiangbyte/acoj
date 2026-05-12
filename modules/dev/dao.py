from typing import Optional, List
from sqlalchemy.orm import Session
from sqlalchemy import select
from .models import GenBasic, GenConfig
from core.db.base_dao import BaseDAO


class GenBasicDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, GenBasic)


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

    def delete_by_basic_ids(self, basic_ids: List[str]) -> int:
        if not basic_ids:
            return 0
        entities = self.db.execute(
            select(GenConfig).where(
                GenConfig.basic_id.in_(basic_ids),
                GenConfig.is_deleted == self._soft_delete_not_deleted
            )
        ).scalars().all()
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
