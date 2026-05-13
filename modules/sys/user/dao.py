from __future__ import annotations
from typing import List, Optional, Dict, Any
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import select, or_
from .models import SysUser, RelUserRole, RelUserGroup, RelUserPermission
from .params import UserPageParam
from core.db.base_dao import BaseDAO
from core.enums import ResourceCategoryEnum, ResourceTypeEnum, StatusEnum, DataScopeEnum
from core.utils import generate_id
from modules.sys.role.params import PermissionItem


class UserDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysUser)

    def find_page(self, param: UserPageParam) -> Dict[str, Any]:
        def builder(query):
            if param.keyword:
                keyword = f"%{param.keyword}%"
                query = query.where(
                    or_(SysUser.account.ilike(keyword), SysUser.nickname.ilike(keyword))
                )
            if param.status:
                query = query.where(SysUser.status == param.status)
            return query.order_by(SysUser.created_at.desc())
        return super().find_page(param, builder)

    def find_by_account(self, account: str) -> Optional[SysUser]:
        return (
            self.db.execute(
                select(SysUser).where(SysUser.account == account, SysUser.is_deleted == self._soft_delete_not_deleted)
            )
            .scalar_one_or_none()
        )

    def find_by_email(self, email: str) -> Optional[SysUser]:
        return (
            self.db.execute(
                select(SysUser).where(SysUser.email == email, SysUser.is_deleted == self._soft_delete_not_deleted)
            )
            .scalar_one_or_none()
        )

    # ---- RAL: User Roles ----

    def get_role_ids_by_user_id(self, user_id: str) -> List[str]:
        rows = self.db.execute(
            select(RelUserRole.role_id).where(RelUserRole.user_id == user_id)
        ).scalars().all()
        return list(rows)

    def get_role_ids_map_by_user_ids(self, user_ids: List[str]) -> Dict[str, List[str]]:
        if not user_ids:
            return {}
        rows = self.db.execute(
            select(RelUserRole.user_id, RelUserRole.role_id).where(
                RelUserRole.user_id.in_(user_ids),
            )
        ).all()
        result: Dict[str, List[str]] = {uid: [] for uid in user_ids}
        for uid, rid in rows:
            result.setdefault(uid, []).append(rid)
        return result

    def grant_roles(self, user_id: str, role_ids: List[str], created_by: Optional[str] = None,
                    scope: Optional[str] = None, custom_scope_group_ids: Optional[str] = None):
        self.db.query(RelUserRole).filter(RelUserRole.user_id == user_id).delete(synchronize_session=False)

        for rid in role_ids:
            rel = RelUserRole(
                id=generate_id(), user_id=user_id, role_id=rid,
                scope=scope, custom_scope_group_ids=custom_scope_group_ids,
            )
            self.db.add(rel)
        self.db.commit()

    # ---- RAL: User Groups ----

    def get_group_ids_by_user_id(self, user_id: str) -> List[str]:
        rows = self.db.execute(
            select(RelUserGroup.group_id).where(RelUserGroup.user_id == user_id)
        ).scalars().all()
        return list(rows)

    def get_group_ids_map_by_user_ids(self, user_ids: List[str]) -> Dict[str, List[str]]:
        if not user_ids:
            return {}
        rows = self.db.execute(
            select(RelUserGroup.user_id, RelUserGroup.group_id).where(
                RelUserGroup.user_id.in_(user_ids),
            )
        ).all()
        result: Dict[str, List[str]] = {uid: [] for uid in user_ids}
        for uid, gid in rows:
            result.setdefault(uid, []).append(gid)
        return result

    def grant_groups(self, user_id: str, group_ids: List[str], created_by: Optional[str] = None):
        self.db.query(RelUserGroup).filter(RelUserGroup.user_id == user_id).delete(synchronize_session=False)

        for gid in group_ids:
            rel = RelUserGroup(
                id=generate_id(), user_id=user_id, group_id=gid,
            )
            self.db.add(rel)
        self.db.commit()

    # ---- RAL: User Permissions (direct) ----

    def get_permission_details_by_user_id(self, user_id: str) -> list[dict]:
        rows = self.db.execute(
            select(
                RelUserPermission.permission_code,
                RelUserPermission.scope,
                RelUserPermission.custom_scope_group_ids,
                RelUserPermission.custom_scope_org_ids,
            ).where(
                RelUserPermission.user_id == user_id,
            )
        ).all()
        return [
            {
                "permission_code": r[0],
                "scope": r[1] or DataScopeEnum.ALL.value,
                "custom_scope_group_ids": r[2],
                "custom_scope_org_ids": r[3],
            }
            for r in rows
        ]

    def grant_permissions(self, user_id: str, permissions: List[PermissionItem], created_by: Optional[str] = None):
        self.db.query(RelUserPermission).filter(RelUserPermission.user_id == user_id).delete(synchronize_session=False)

        for p in permissions:
            rel = RelUserPermission(
                id=generate_id(), user_id=user_id, permission_code=p.permission_code,
                scope=p.scope, custom_scope_group_ids=p.custom_scope_group_ids,
                custom_scope_org_ids=p.custom_scope_org_ids,
            )
            self.db.add(rel)
        self.db.commit()

    # ---- Cross-table auth queries ----

    def get_user_role_ids_all_sources(self, user_id: str) -> List[str]:
        """Get role IDs from direct assignment + org membership."""
        role_ids: set[str] = set()

        # Direct role assignments (RelUserRole)
        direct_rows = self.db.execute(
            select(RelUserRole.role_id).where(RelUserRole.user_id == user_id)
        ).scalars().all()
        role_ids.update(direct_rows)

        # Via org (RelOrgRole)
        from ..org.models import RelOrgRole as _RelOrgRole
        entity = self.find_by_id(user_id)
        if entity and entity.org_id:
            org_rows = self.db.execute(
                select(_RelOrgRole.role_id).where(
                    _RelOrgRole.org_id == entity.org_id,
                )
            ).scalars().all()
            role_ids.update(org_rows)

        return list(role_ids)

    def get_role_resource_ids(self, role_ids: List[str]) -> List[str]:
        from ..role.models import RelRoleResource as _RelRoleResource
        rows = self.db.execute(
            select(_RelRoleResource.resource_id).where(
                _RelRoleResource.role_id.in_(role_ids),
            )
        ).scalars().all()
        return list(set(rows))

    def get_resources_by_ids(self, resource_ids: List[str]):
        """Get backend menu resources by IDs, ordered by sort_code."""
        from ..resource.models import SysResource as _SysResource
        rows = (
            self.db.query(_SysResource)
            .filter(
                _SysResource.id.in_(resource_ids),
                _SysResource.category == ResourceCategoryEnum.BACKEND_MENU,
                _SysResource.type.in_([ResourceTypeEnum.DIRECTORY, ResourceTypeEnum.MENU]),
                _SysResource.status == StatusEnum.ENABLED,
                _SysResource.is_deleted == self._soft_delete_not_deleted,
            )
            .order_by(_SysResource.sort_code.asc())
            .all()
        )
        return rows

    def get_role_permission_codes(self, role_ids: List[str]) -> List[str]:
        from ..role.models import RelRolePermission as _RelRolePermission
        rows = self.db.execute(
            select(_RelRolePermission.permission_code).where(
                _RelRolePermission.role_id.in_(role_ids),
            )
        ).scalars().all()
        return list(set(rows))
