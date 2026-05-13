from typing import Optional, List, Dict, Any
from sqlalchemy.orm import Session
from sqlalchemy import select, delete
from core.db.base_dao import BaseDAO
from core.db.query_wrapper import QueryWrapper
from .models import SysFile
from .params import FilePageParam


class FileDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysFile)

    def find_page_by_filters(self, param: FilePageParam) -> Dict[str, Any]:
        wrapper = QueryWrapper(SysFile)
        if param.engine:
            wrapper.eq(SysFile.engine, param.engine)
        if param.keyword:
            wrapper.like(SysFile.name, param.keyword)
        if param.date_range_start:
            wrapper.ge(SysFile.created_at, param.date_range_start)
        if param.date_range_end:
            wrapper.le(SysFile.created_at, param.date_range_end)
        wrapper.order_by_desc(SysFile.created_at)
        return self.select_page(wrapper, param)

    def delete_absolute_by_id(self, entity_id: str) -> bool:
        entity = self.db.get(self.model, entity_id)
        if not entity:
            return False
        self.db.delete(entity)
        self.db.commit()
        return True
