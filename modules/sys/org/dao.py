from typing import List, Optional
from sqlalchemy import select
from sqlalchemy.orm import Session
from .models import SysOrg, RalOrgRole
from core.db.base_dao import BaseDAO
from core.utils import generate_id
from datetime import datetime


class OrgDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysOrg)

    def insert(self, entity: SysOrg) -> SysOrg:
        entity.id = generate_id()
        if self._can_apply_soft_delete():
            setattr(entity, self._soft_delete_field, self._soft_delete_not_deleted)
        now = datetime.now()
        entity.created_at = now
        entity.updated_at = now
        self.db.add(entity)
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def insert_batch(self, entities: List[SysOrg]) -> None:
        now = datetime.now()
        for entity in entities:
            entity.id = generate_id()
            if self._can_apply_soft_delete():
                setattr(entity, self._soft_delete_field, self._soft_delete_not_deleted)
            entity.created_at = now
            entity.updated_at = now
        self.db.add_all(entities)
        self.db.commit()

    def update(self, entity: SysOrg) -> SysOrg:
        entity.updated_at = datetime.now()
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def get_role_ids_by_org_id(self, org_id: str) -> List[str]:
        rows = self.db.execute(
            select(RalOrgRole.role_id).where(
                RalOrgRole.org_id == org_id, RalOrgRole.is_deleted == self._soft_delete_not_deleted
            )
        ).scalars().all()
        return list(rows)

    def grant_roles(self, org_id: str, role_ids: List[str], created_by: Optional[str] = None,
                    scope: Optional[str] = None, custom_scope_group_ids: Optional[str] = None):
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
