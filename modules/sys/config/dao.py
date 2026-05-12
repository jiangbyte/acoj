from typing import Optional, List
from sqlalchemy.orm import Session
from sqlalchemy import select
from core.db.base_dao import BaseDAO
from .models import SysConfig
from .params import ConfigPageParam


class ConfigDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysConfig)

    def find_by_key(self, key: str) -> Optional[SysConfig]:
        return self.find_by_field("config_key", key)

    def find_page(self, param: ConfigPageParam) -> dict:
        def builder(query):
            if param.category:
                query = query.where(self.model.category == param.category)
            if param.keyword:
                query = query.where(self.model.config_key.like(f"%{param.keyword}%"))
            return query
        return super().find_page(param, builder)

    def find_by_category(self, category: str) -> List[SysConfig]:
        query = select(self.model).where(self.model.category == category)
        query = self._apply_soft_delete_filter(query)
        return list(self.db.execute(query).scalars().all())

    def find_by_category_and_key(self, category: str, key: str) -> Optional[SysConfig]:
        query = select(self.model).where(
            self.model.category == category,
            self.model.config_key == key,
        )
        query = self._apply_soft_delete_filter(query)
        return self.db.execute(query).scalar_one_or_none()
