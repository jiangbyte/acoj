from typing import Dict, Any
from sqlalchemy.orm import Session
from .models import SysLog
from .params import LogPageParam
from core.db.base_dao import BaseDAO
from core.db.query_wrapper import QueryWrapper


class LogDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysLog)

    def find_page_by_filters(self, param: LogPageParam) -> Dict[str, Any]:
        wrapper = QueryWrapper(SysLog)
        if param.keyword:
            wrapper.like(SysLog.name, param.keyword)
        if param.category:
            wrapper.eq(SysLog.category, param.category)
        if param.exe_status:
            wrapper.eq(SysLog.exe_status, param.exe_status)
        wrapper.order_by_desc(SysLog.op_time)
        return self.select_page(wrapper, param)
