from typing import Dict, Any, List
from sqlalchemy.orm import Session
from sqlalchemy import or_
from .models import SysGroup
from .params import GroupPageParam
from core.db.base_dao import BaseDAO
from core.db.query_wrapper import QueryWrapper


class GroupDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysGroup)

    def find_page_by_filters(self, param: GroupPageParam) -> Dict[str, Any]:
        wrapper = QueryWrapper(SysGroup)
        if param.parent_id:
            wrapper.where(or_(SysGroup.parent_id == param.parent_id, SysGroup.id == param.parent_id))
        if param.org_id:
            wrapper.eq(SysGroup.org_id, param.org_id)
        if param.keyword:
            wrapper.like(SysGroup.name, param.keyword)
        wrapper.order_by_asc(SysGroup.sort_code)
        return self.select_page(wrapper, param)

    def find_all_ordered(self) -> List[SysGroup]:
        wrapper = QueryWrapper(SysGroup).order_by_asc(SysGroup.sort_code)
        return self.select_list(wrapper)

    def find_all_groups_ordered(self) -> List[SysGroup]:
        return self.find_all_ordered()
