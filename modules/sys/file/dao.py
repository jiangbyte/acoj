from typing import Optional, List
from sqlalchemy.orm import Session
from sqlalchemy import select, func, delete
from core.db.base_dao import BaseDAO
from .models import SysFile


class FileDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysFile)

    def find_page(self, engine: Optional[str] = None,
                  keyword: Optional[str] = None,
                  date_range_start: Optional[str] = None,
                  date_range_end: Optional[str] = None,
                  current: int = 1, size: int = 10) -> dict:
        query = select(self.model)
        query = self._apply_soft_delete_filter(query)

        if engine:
            query = query.where(self.model.engine == engine)
        if keyword:
            query = query.where(self.model.name.like(f"%{keyword}%"))
        if date_range_start:
            query = query.where(self.model.created_at >= date_range_start)
        if date_range_end:
            query = query.where(self.model.created_at <= date_range_end)

        query = query.order_by(self.model.created_at.desc())

        count_query = select(func.count()).select_from(query.subquery())
        total = self.db.execute(count_query).scalar() or 0

        current = max(1, current)
        offset = (current - 1) * size
        records = list(self.db.execute(query.offset(offset).limit(size)).scalars().all())

        return {"records": records, "total": total}

    def delete_absolute_by_id(self, entity_id: str) -> bool:
        entity = self.db.get(self.model, entity_id)
        if not entity:
            return False
        self.db.delete(entity)
        self.db.commit()
        return True
