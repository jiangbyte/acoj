from __future__ import annotations
from typing import List, Optional, Dict, Any
from datetime import datetime
from sqlalchemy import select, func, delete as sa_delete
from sqlalchemy.orm import Session
from plugins.plugin_sys.shared import SUPER_ADMIN_CODE
from .models import SysRole, RelRolePermission, RelRoleResource
from .params import PermissionItem, RolePageParam
from sdk.utils import generate_id


class RoleRepository:
    def __init__(self, db: Session):
        self.db = db

    # ---- base CRUD ----

    def find_by_id(self, id: str) -> Optional[SysRole]:
        return self.db.execute(select(SysRole).where(SysRole.id == id)).scalar_one_or_none()

    def find_by_ids(self, ids: List[str]) -> List[SysRole]:
        if not ids:
            return []
        return list(self.db.execute(select(SysRole).where(SysRole.id.in_(ids))).scalars().all())

    def exists_super_admin(self, role_ids: List[str]) -> bool:
        if not role_ids:
            return False
        stmt = select(func.count()).select_from(SysRole).where(
            SysRole.id.in_(role_ids),
            SysRole.code == SUPER_ADMIN_CODE,
        )
        return int(self.db.execute(stmt).scalar() or 0) > 0

    def find_page(self, param: RolePageParam) -> Dict[str, Any]:
        current = max(1, param.current)
        size = max(1, param.size)
        offset = (current - 1) * size
        stmt = select(SysRole)
        if param.keyword:
            like = f"%{param.keyword}%"
            stmt = stmt.where(SysRole.name.like(like) | SysRole.code.like(like))
        if param.category:
            stmt = stmt.where(SysRole.category == param.category)
        count_stmt = select(func.count()).select_from(stmt.subquery())
        total = self.db.execute(count_stmt).scalar() or 0
        records = list(self.db.execute(stmt.offset(offset).limit(size)).scalars().all())
        return {"records": records, "total": total}

    def insert(self, entity: SysRole, user_id: Optional[str] = None) -> SysRole:
        from sdk.utils.snowflake_utils import generate_id as gen_snowflake
        now = datetime.now()
        if not entity.id:
            entity.id = gen_snowflake()
        if entity.created_at is None:
            entity.created_at = now
        entity.updated_at = now
        if user_id is not None and entity.created_by is None:
            entity.created_by = user_id
        self.db.add(entity)
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def update(self, entity: SysRole, user_id: Optional[str] = None) -> SysRole:
        entity.updated_at = datetime.now()
        if user_id is not None:
            entity.updated_by = user_id
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def delete_by_ids(self, ids: List[str]) -> int:
        if not ids:
            return 0
        stmt = sa_delete(SysRole).where(SysRole.id.in_(ids))
        affected = self.db.execute(stmt).rowcount
        self.db.commit()
        return affected

    # ---- custom ----

    def get_permission_codes_by_role_id(self, role_id: str) -> List[str]:
        rows = self.db.execute(
            select(RelRolePermission.permission_code).where(
                RelRolePermission.role_id == role_id
            )
        ).scalars().all()
        return list(rows)

    def get_permission_details_by_role_id(self, role_id: str) -> List[PermissionItem]:
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
            PermissionItem(
                permission_code=r[0],
                scope=r[1] or "ALL",
                custom_scope_group_ids=r[2],
                custom_scope_org_ids=r[3],
            )
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

    def add_missing_permissions(self, role_id: str, permissions: List[PermissionItem]):
        existing = set(self.get_permission_codes_by_role_id(role_id))
        for p in permissions:
            if p.permission_code in existing:
                continue
            rel = RelRolePermission(
                id=generate_id(), role_id=role_id, permission_code=p.permission_code,
                scope=p.scope, custom_scope_group_ids=p.custom_scope_group_ids,
                custom_scope_org_ids=p.custom_scope_org_ids,
            )
            self.db.add(rel)
        self.db.commit()

    def get_resource_ids_by_role_id(self, role_id: str) -> List[str]:
        rows = self.db.execute(
            select(RelRoleResource.resource_id).where(
                RelRoleResource.role_id == role_id
            )
        ).scalars().all()
        return list(rows)

    def grant_resources(self, role_id: str, resource_ids: List[str], created_by: Optional[str] = None):
        resource_ids = list(dict.fromkeys(resource_ids))

        self.db.execute(sa_delete(RelRoleResource).where(RelRoleResource.role_id == role_id))

        for resid in resource_ids:
            rel = RelRoleResource(
                id=generate_id(), role_id=role_id, resource_id=resid,
            )
            self.db.add(rel)
        self.db.commit()

    def find_resources_with_extra_by_ids(self, resource_ids: List[str]):
        from ..resource.models import SysResource as _SR
        stmt = (
            select(_SR)
            .where(
                _SR.id.in_(resource_ids),
                _SR.extra.is_not(None),
                _SR.extra != "",
            )
        )
        return list(self.db.execute(stmt).scalars().all())
