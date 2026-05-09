from typing import Optional, List
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import select, func
from core.db.base_dao import BaseDAO
from .models import SysConfig


class ConfigDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysConfig)

    def find_by_key(self, key: str) -> Optional[SysConfig]:
        return self.find_by_field("config_key", key)

    def find_page(self, category: Optional[str] = None,
                  keyword: Optional[str] = None,
                  current: int = 1, size: int = 10) -> dict:
        query = select(self.model)
        query = self._apply_soft_delete_filter(query)

        if category:
            query = query.where(self.model.category == category)
        if keyword:
            query = query.where(self.model.config_key.like(f"%{keyword}%"))

        count_query = select(func.count()).select_from(query.subquery())
        total = self.db.execute(count_query).scalar() or 0

        current = max(1, current)
        offset = (current - 1) * size
        records = list(self.db.execute(query.offset(offset).limit(size)).scalars().all())

        return {"records": records, "total": total}

    def find_by_category(self, category: str) -> List[SysConfig]:
        query = select(self.model).where(self.model.category == category)
        query = self._apply_soft_delete_filter(query)
        return list(self.db.execute(query).scalars().all())
