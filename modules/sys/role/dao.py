from __future__ import annotations
from typing import List, Optional
from datetime import datetime
from sqlalchemy import select, delete as sa_delete
from sqlalchemy.orm import Session
from .models import SysRole, RelRolePermission, RelRoleResource
from .params import PermissionItem
from core.db.base_dao import BaseDAO
from core.utils import generate_id


class RoleDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysRole)

    # ---- RAL: Role Permissions ----

    def get_permission_codes_by_role_id(self, role_id: str) -> List[str]:
        rows = self.db.execute(
            select(RelRolePermission.permission_code).where(
                RelRolePermission.role_id == role_id
            )
        ).scalars().all()
        return list(rows)

    def get_permission_details_by_role_id(self, role_id: str) -> list[dict]:
        rows = self.db.execute(
            select(
                RelRolePermission.permission_code,
                RelRolePermission.scope,
                RelRolePermission.custom_scope_group_ids,
                RelRolePermission.custom_scope_org_ids,
            ).where(
                RelRolePermission.role_id == role_id,
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

    def grant_permissions(self, role_id: str, permissions: List[PermissionItem], created_by: Optional[str] = None):
        self.db.execute(sa_delete(RelRolePermission).where(RelRolePermission.role_id == role_id))

        for p in permissions:
            rel = RelRolePermission(
                id=generate_id(), role_id=role_id, permission_code=p.permission_code,
                scope=p.scope, custom_scope_group_ids=p.custom_scope_group_ids,
                custom_scope_org_ids=p.custom_scope_org_ids,
            )
            self.db.add(rel)
        self.db.commit()

    # ---- RAL: Role Resources ----

    def get_resource_ids_by_role_id(self, role_id: str) -> List[str]:
        rows = self.db.execute(
            select(RelRoleResource.resource_id).where(
                RelRoleResource.role_id == role_id
            )
        ).scalars().all()
        return list(rows)

    def grant_resources(self, role_id: str, resource_ids: List[str], created_by: Optional[str] = None):
        resource_ids = list(dict.fromkeys(resource_ids))  # deduplicate

        self.db.execute(sa_delete(RelRoleResource).where(RelRoleResource.role_id == role_id))

        for resid in resource_ids:
            rel = RelRoleResource(
                id=generate_id(), role_id=role_id, resource_id=resid,
            )
            self.db.add(rel)
        self.db.commit()

    # ---- Cross-table queries ----

    def find_resources_with_extra_by_ids(self, resource_ids: List[str]):
        """Find resources with non-empty extra field by IDs (for auto-granting permissions)."""
        from ..resource.models import SysResource as _SR
        stmt = (
            select(_SR)
            .where(
                _SR.id.in_(resource_ids),
                _SR.is_deleted == self._soft_delete_not_deleted,
                _SR.extra != None,
                _SR.extra != "",
            )
        )
        return list(self.db.execute(stmt).scalars().all())
