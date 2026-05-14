from typing import List, Optional, Dict, Any
from sqlalchemy import select, or_, delete as sa_delete
from sqlalchemy.orm import Session
from .models import SysOrg, RelOrgRole
from .params import OrgPageParam
from core.db.base_dao import BaseDAO
from core.db.query_wrapper import QueryWrapper


class OrgDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysOrg)

    def find_page_by_filters(self, param: OrgPageParam) -> Dict[str, Any]:
        wrapper = QueryWrapper(SysOrg)
        if param.parent_id:
            wrapper.where(or_(SysOrg.parent_id == param.parent_id, SysOrg.id == param.parent_id))
        if param.keyword:
            wrapper.like(SysOrg.name, param.keyword)
        wrapper.order_by_asc(SysOrg.sort_code)
        return self.select_page(wrapper, param)

    def find_all_ordered(self) -> List[SysOrg]:
        wrapper = QueryWrapper(SysOrg).order_by_asc(SysOrg.sort_code)
        return self.select_list(wrapper)

    def get_role_ids_by_org_id(self, org_id: str) -> List[str]:
        rows = self.db.execute(
            select(RelOrgRole.role_id).where(RelOrgRole.org_id == org_id)
        ).scalars().all()
        return list(rows)

    def grant_roles(self, org_id: str, role_ids: List[str], created_by: Optional[str] = None,
                    scope: Optional[str] = None, custom_scope_group_ids: Optional[str] = None):
        from core.utils import generate_id

        self.db.execute(sa_delete(RelOrgRole).where(RelOrgRole.org_id == org_id))

        for rid in role_ids:
            rel = RelOrgRole(
                id=generate_id(), org_id=org_id, role_id=rid,
                scope=scope, custom_scope_group_ids=custom_scope_group_ids,
            )
            self.db.add(rel)
        self.db.commit()
