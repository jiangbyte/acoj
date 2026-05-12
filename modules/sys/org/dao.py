from typing import List, Optional, Dict, Any
from sqlalchemy import select, or_
from sqlalchemy.orm import Session
from .models import SysOrg, RalOrgRole
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
            select(RalOrgRole.role_id).where(
                RalOrgRole.org_id == org_id, RalOrgRole.is_deleted == self._soft_delete_not_deleted
            )
        ).scalars().all()
        return list(rows)

    def grant_roles(self, org_id: str, role_ids: List[str], created_by: Optional[str] = None,
                    scope: Optional[str] = None, custom_scope_group_ids: Optional[str] = None):
        from datetime import datetime
        from core.utils import generate_id

        now = datetime.now()
        not_del = self._soft_delete_not_deleted
        del_val = self._soft_delete_deleted

        existing = self.db.execute(
            select(RalOrgRole).where(RalOrgRole.org_id == org_id)
        ).scalars().all()
        existing_by_rid = {r.role_id: r for r in existing}

        for r in existing:
            if r.role_id not in role_ids and r.is_deleted == not_del:
                r.is_deleted = del_val

        for rid in role_ids:
            if rid in existing_by_rid:
                rel = existing_by_rid[rid]
                rel.is_deleted = not_del
                rel.scope = scope
                rel.custom_scope_group_ids = custom_scope_group_ids
                rel.created_by = created_by
            else:
                rel = RalOrgRole(
                    id=generate_id(), org_id=org_id, role_id=rid,
                    scope=scope, custom_scope_group_ids=custom_scope_group_ids,
                    is_deleted=not_del, created_at=now, created_by=created_by
                )
                self.db.add(rel)
        self.db.commit()
