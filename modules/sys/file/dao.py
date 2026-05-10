from typing import Optional, List
from sqlalchemy.orm import Session
from sqlalchemy import select, delete
from core.db.base_dao import BaseDAO
from .models import SysFile
from .params import FilePageParam


class FileDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysFile)

    def find_page(self, param: FilePageParam) -> dict:
        def builder(query):
            if param.engine:
                query = query.where(self.model.engine == param.engine)
            if param.keyword:
                query = query.where(self.model.name.like(f"%{param.keyword}%"))
            if param.date_range_start:
                query = query.where(self.model.created_at >= param.date_range_start)
            if param.date_range_end:
                query = query.where(self.model.created_at <= param.date_range_end)
            return query.order_by(self.model.created_at.desc())
        return super().find_page(param, builder)

    def delete_absolute_by_id(self, entity_id: str) -> bool:
        entity = self.db.get(self.model, entity_id)
        if not entity:
            return False
        self.db.delete(entity)
        self.db.commit()
        return True
