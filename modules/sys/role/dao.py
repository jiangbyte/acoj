from typing import List, Optional
from sqlalchemy.orm import Session
from sqlalchemy import select
from .models import SysRole, RalRolePermission, RalRoleResource
from core.db.base_dao import BaseDAO
from core.utils import generate_id
from datetime import datetime


class RoleDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysRole)

    def insert(self, entity: SysRole) -> SysRole:
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

    def insert_batch(self, entities: List[SysRole]) -> None:
        now = datetime.now()
        for entity in entities:
            entity.id = generate_id()
            if self._can_apply_soft_delete():
                setattr(entity, self._soft_delete_field, self._soft_delete_not_deleted)
            entity.created_at = now
            entity.updated_at = now
        self.db.add_all(entities)
        self.db.commit()

    def update(self, entity: SysRole) -> SysRole:
        entity.updated_at = datetime.now()
        self.db.commit()
        self.db.refresh(entity)
        return entity

    # ---- RAL: Role Permissions ----
    def get_permission_codes_by_role_id(self, role_id: str) -> List[str]:
        rows = self.db.execute(
            select(RalRolePermission.permission_code).where(
                RalRolePermission.role_id == role_id, RalRolePermission.is_deleted == self._soft_delete_not_deleted
            )
        ).scalars().all()
        return list(rows)

    def get_permission_details_by_role_id(self, role_id: str) -> list[dict]:
        """Return permission_code + scope + custom IDs for a role."""
        rows = self.db.execute(
            select(
                RalRolePermission.permission_code,
                RalRolePermission.scope,
                RalRolePermission.custom_scope_group_ids,
                RalRolePermission.custom_scope_org_ids,
            ).where(
                RalRolePermission.role_id == role_id,
                RalRolePermission.is_deleted == self._soft_delete_not_deleted
            )
        ).all()
        return [
            {
                "permission_code": r[0],
                "scope": r[1] or "ALL",
                "custom_scope_group_ids": r[2],
                "custom_scope_org_ids": r[3],
            }
            for r in rows
        ]

    def grant_permissions(self, role_id: str, permissions: List['PermissionItem'], created_by: Optional[str] = None):
        from ..params import PermissionItem
        now = datetime.now()
        not_del = self._soft_delete_not_deleted
        del_val = self._soft_delete_deleted

        incoming_codes = [p.permission_code for p in permissions]

        existing = self.db.execute(
            select(RalRolePermission).where(RalRolePermission.role_id == role_id)
        ).scalars().all()
        existing_by_code = {r.permission_code: r for r in existing}

        for r in existing:
            if r.permission_code not in incoming_codes and r.is_deleted == not_del:
                r.is_deleted = del_val

        for p in permissions:
            if p.permission_code in existing_by_code:
                rel = existing_by_code[p.permission_code]
                rel.is_deleted = not_del
                rel.scope = p.scope
                rel.custom_scope_group_ids = p.custom_scope_group_ids
                rel.custom_scope_org_ids = p.custom_scope_org_ids
                rel.created_by = created_by
            else:
                rel = RalRolePermission(
                    id=generate_id(), role_id=role_id, permission_code=p.permission_code,
                    scope=p.scope, custom_scope_group_ids=p.custom_scope_group_ids,
                    custom_scope_org_ids=p.custom_scope_org_ids,
                    is_deleted=not_del, created_at=now, created_by=created_by
                )
                self.db.add(rel)
        self.db.commit()

    # ---- RAL: Role Resources ----
    def get_resource_ids_by_role_id(self, role_id: str) -> List[str]:
        rows = self.db.execute(
            select(RalRoleResource.resource_id).where(
                RalRoleResource.role_id == role_id, RalRoleResource.is_deleted == self._soft_delete_not_deleted
            )
        ).scalars().all()
        return list(rows)

    def grant_resources(self, role_id: str, resource_ids: List[str], created_by: Optional[str] = None):
        now = datetime.now()
        not_del = self._soft_delete_not_deleted
        del_val = self._soft_delete_deleted
        resource_ids = list(dict.fromkeys(resource_ids))  # deduplicate

        existing = self.db.execute(
            select(RalRoleResource).where(RalRoleResource.role_id == role_id)
        ).scalars().all()
        existing_by_resid = {r.resource_id: r for r in existing}

        for r in existing:
            if r.resource_id not in resource_ids and r.is_deleted == not_del:
                r.is_deleted = del_val

        for resid in resource_ids:
            if resid in existing_by_resid:
                rel = existing_by_resid[resid]
                rel.is_deleted = not_del
                rel.created_by = created_by
            else:
                rel = RalRoleResource(
                    id=generate_id(), role_id=role_id, resource_id=resid,
                    is_deleted=not_del, created_at=now, created_by=created_by
                )
                self.db.add(rel)
        self.db.commit()
