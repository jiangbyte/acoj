from typing import Optional, Dict, Any
from sqlalchemy.orm import Session
from .models import SysPosition
from .params import PositionPageParam
from core.db.base_dao import BaseDAO
from core.db.query_wrapper import QueryWrapper


class PositionDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysPosition)

    def find_page_by_filters(self, param: PositionPageParam) -> Dict[str, Any]:
        wrapper = QueryWrapper(SysPosition)
        if param.group_id:
            wrapper.eq(SysPosition.group_id, param.group_id)
        if param.org_id:
            wrapper.eq(SysPosition.org_id, param.org_id)
        if param.keyword:
            wrapper.like(SysPosition.name, param.keyword)
        wrapper.order_by_asc(SysPosition.sort_code)
        return self.select_page(wrapper, param)
