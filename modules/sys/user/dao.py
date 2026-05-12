from __future__ import annotations
from typing import List, Optional, Dict, Any
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import select, or_
from .models import SysUser, RalUserRole, RalUserGroup, RalUserPermission
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
            select(RalUserRole.role_id).where(
                RalUserRole.user_id == user_id, RalUserRole.is_deleted == self._soft_delete_not_deleted
            )
        ).scalars().all()
        return list(rows)

    def get_role_ids_map_by_user_ids(self, user_ids: List[str]) -> Dict[str, List[str]]:
        if not user_ids:
            return {}
        rows = self.db.execute(
            select(RalUserRole.user_id, RalUserRole.role_id).where(
                RalUserRole.user_id.in_(user_ids),
                RalUserRole.is_deleted == self._soft_delete_not_deleted
            )
        ).all()
        result: Dict[str, List[str]] = {uid: [] for uid in user_ids}
        for uid, rid in rows:
            result.setdefault(uid, []).append(rid)
        return result

    def grant_roles(self, user_id: str, role_ids: List[str], created_by: Optional[str] = None,
                    scope: Optional[str] = None, custom_scope_group_ids: Optional[str] = None):
        now = datetime.now()
        not_del = self._soft_delete_not_deleted
        del_val = self._soft_delete_deleted

        existing = self.db.execute(
            select(RalUserRole).where(RalUserRole.user_id == user_id)
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
                rel = RalUserRole(
                    id=generate_id(), user_id=user_id, role_id=rid,
                    scope=scope, custom_scope_group_ids=custom_scope_group_ids,
                    is_deleted=not_del, created_at=now, created_by=created_by
                )
                self.db.add(rel)
        self.db.commit()

    # ---- RAL: User Groups ----

    def get_group_ids_by_user_id(self, user_id: str) -> List[str]:
        rows = self.db.execute(
            select(RalUserGroup.group_id).where(
                RalUserGroup.user_id == user_id, RalUserGroup.is_deleted == self._soft_delete_not_deleted
            )
        ).scalars().all()
        return list(rows)

    def get_group_ids_map_by_user_ids(self, user_ids: List[str]) -> Dict[str, List[str]]:
        if not user_ids:
            return {}
        rows = self.db.execute(
            select(RalUserGroup.user_id, RalUserGroup.group_id).where(
                RalUserGroup.user_id.in_(user_ids),
                RalUserGroup.is_deleted == self._soft_delete_not_deleted
            )
        ).all()
        result: Dict[str, List[str]] = {uid: [] for uid in user_ids}
        for uid, gid in rows:
            result.setdefault(uid, []).append(gid)
        return result

    def grant_groups(self, user_id: str, group_ids: List[str], created_by: Optional[str] = None):
        now = datetime.now()
        not_del = self._soft_delete_not_deleted
        del_val = self._soft_delete_deleted

        existing = self.db.execute(
            select(RalUserGroup).where(RalUserGroup.user_id == user_id)
        ).scalars().all()
        existing_by_gid = {r.group_id: r for r in existing}

        for r in existing:
            if r.group_id not in group_ids and r.is_deleted == not_del:
                r.is_deleted = del_val

        for gid in group_ids:
            if gid in existing_by_gid:
                rel = existing_by_gid[gid]
                rel.is_deleted = not_del
                rel.created_by = created_by
            else:
                rel = RalUserGroup(
                    id=generate_id(), user_id=user_id, group_id=gid,
                    is_deleted=not_del, created_at=now, created_by=created_by
                )
                self.db.add(rel)
        self.db.commit()

    # ---- RAL: User Permissions (direct) ----

    def get_permission_details_by_user_id(self, user_id: str) -> list[dict]:
        rows = self.db.execute(
            select(
                RalUserPermission.permission_code,
                RalUserPermission.scope,
                RalUserPermission.custom_scope_group_ids,
                RalUserPermission.custom_scope_org_ids,
            ).where(
                RalUserPermission.user_id == user_id,
                RalUserPermission.is_deleted == self._soft_delete_not_deleted
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
        now = datetime.now()
        not_del = self._soft_delete_not_deleted
        del_val = self._soft_delete_deleted

        incoming_codes = [p.permission_code for p in permissions]

        existing = self.db.execute(
            select(RalUserPermission).where(RalUserPermission.user_id == user_id)
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
                rel = RalUserPermission(
                    id=generate_id(), user_id=user_id, permission_code=p.permission_code,
                    scope=p.scope, custom_scope_group_ids=p.custom_scope_group_ids,
                    custom_scope_org_ids=p.custom_scope_org_ids,
                    is_deleted=not_del, created_at=now, created_by=created_by
                )
                self.db.add(rel)
        self.db.commit()

    # ---- Cross-table auth queries ----

    def get_user_role_ids_all_sources(self, user_id: str) -> List[str]:
        """Get role IDs from direct assignment + org membership."""
        role_ids: set[str] = set()

        # Direct role assignments (RalUserRole)
        direct_rows = self.db.execute(
            select(RalUserRole.role_id).where(
                RalUserRole.user_id == user_id, RalUserRole.is_deleted == self._soft_delete_not_deleted
            )
        ).scalars().all()
        role_ids.update(direct_rows)

        # Via org (RalOrgRole)
        from ..org.models import RalOrgRole as _RalOrgRole
        entity = self.find_by_id(user_id)
        if entity and entity.org_id:
            org_rows = self.db.execute(
                select(_RalOrgRole.role_id).where(
                    _RalOrgRole.org_id == entity.org_id,
                    _RalOrgRole.is_deleted == self._soft_delete_not_deleted,
                )
            ).scalars().all()
            role_ids.update(org_rows)

        return list(role_ids)

    def get_role_resource_ids(self, role_ids: List[str]) -> List[str]:
        from ..role.models import RalRoleResource as _RalRoleResource
        rows = self.db.execute(
            select(_RalRoleResource.resource_id).where(
                _RalRoleResource.role_id.in_(role_ids),
                _RalRoleResource.is_deleted == self._soft_delete_not_deleted,
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
                _SysResource.status == StatusEnum.ENABLE,
                _SysResource.is_deleted == self._soft_delete_not_deleted,
            )
            .order_by(_SysResource.sort_code.asc())
            .all()
        )
        return rows

    def get_role_permission_codes(self, role_ids: List[str]) -> List[str]:
        from ..role.models import RalRolePermission as _RalRolePermission
        rows = self.db.execute(
            select(_RalRolePermission.permission_code).where(
                _RalRolePermission.role_id.in_(role_ids),
                _RalRolePermission.is_deleted == self._soft_delete_not_deleted,
            )
        ).scalars().all()
        return list(set(rows))
