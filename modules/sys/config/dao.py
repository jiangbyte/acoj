from typing import Optional, List, Dict, Any
from sqlalchemy.orm import Session
from sqlalchemy import select
from core.db.base_dao import BaseDAO
from core.db.query_wrapper import QueryWrapper
from .models import SysConfig
from .params import ConfigPageParam


class ConfigDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysConfig)

    def find_by_key(self, key: str) -> Optional[SysConfig]:
        return self.find_by_field("config_key", key)

    def find_page_by_filters(self, param: ConfigPageParam) -> Dict[str, Any]:
        wrapper = QueryWrapper(SysConfig)
        if param.category:
            wrapper.eq(SysConfig.category, param.category)
        if param.keyword:
            wrapper.like(SysConfig.config_key, param.keyword)
        return self.select_page(wrapper, param)

    def find_by_category(self, category: str) -> List[SysConfig]:
        stmt = select(self.model).where(self.model.category == category)
        stmt = self._apply_soft_delete_filter(stmt)
        return list(self.db.execute(stmt).scalars().all())

    def find_by_category_and_key(self, category: str, key: str) -> Optional[SysConfig]:
        stmt = select(self.model).where(
            self.model.category == category,
            self.model.config_key == key,
        )
        stmt = self._apply_soft_delete_filter(stmt)
        return self.db.execute(stmt).scalar_one_or_none()

    def find_by_category_and_keys(self, category: str, keys: List[str]) -> Dict[str, SysConfig]:
        stmt = select(self.model).where(
            self.model.category == category,
            self.model.config_key.in_(keys),
        )
        stmt = self._apply_soft_delete_filter(stmt)
        return {r.config_key: r for r in self.db.execute(stmt).scalars().all()}

    def find_by_keys(self, keys: List[str]) -> Dict[str, SysConfig]:
        stmt = select(self.model).where(
            self.model.config_key.in_(keys),
        )
        stmt = self._apply_soft_delete_filter(stmt)
        return {r.config_key: r for r in self.db.execute(stmt).scalars().all()}
