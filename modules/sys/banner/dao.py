from typing import Optional, List
from sqlalchemy.orm import Session
from sqlalchemy import select
from .models import SysBanner
from core.db.base_dao import BaseDAO


class BannerDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysBanner)

    def find_by_code(self, code: str) -> Optional[SysBanner]:
        query = select(SysBanner).where(SysBanner.code == code)
        query = self._apply_soft_delete_filter(query)
        return self.db.execute(query).scalar_one_or_none()
