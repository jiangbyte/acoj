"""
PermissionProvider — runtime permission loader.

Mirrors hei-gin's ``plugins/plugin-sys/provider/permission_provider.go``.
"""

from __future__ import annotations

import json
import logging
from typing import Optional

from core.constants import PERMISSION_CACHE_KEY, SUPER_ADMIN_CODE
from core.db import SessionLocal
from core.enums import LoginTypeEnum, DataScopeEnum, PermissionPathEnum

logger = logging.getLogger(__name__)


class PermissionProvider:
    """Provides PermissionList, RoleList, PermissionScopeMap lookups.

    Mirrors hei-gin's provider/permission_provider.go.
    """

    def _get_role_ids(self, login_id: str) -> list[str]:
        from plugins.plugin_sys.user.models import RelUserRole

        db = SessionLocal()
        try:
            rows = db.query(RelUserRole.role_id).filter(RelUserRole.user_id == login_id).all()
            return [r[0] for r in rows]
        finally:
            db.close()

    def _get_roles_by_ids(self, role_ids: list[str]):
        from plugins.plugin_sys.role.models import SysRole

        if not role_ids:
            return []
        db = SessionLocal()
        try:
            return db.query(SysRole).filter(SysRole.id.in_(role_ids)).all()
        finally:
            db.close()

    def _is_super_admin(self, role_ids: list[str]) -> bool:
        from plugins.plugin_sys.role.models import SysRole

        if not role_ids:
            return False
        db = SessionLocal()
        try:
            return db.query(SysRole).filter(
                SysRole.id.in_(role_ids), SysRole.code == SUPER_ADMIN_CODE
            ).count() > 0
        finally:
            db.close()

    def _get_all_permissions_from_redis(self) -> list[str]:
        from core.db.redis import get_client

        redis_client = get_client()
        if not redis_client:
            return []
        try:
            data = redis_client.get(PERMISSION_CACHE_KEY)
            if not data:
                return []
            tree = json.loads(data)
            codes = []
            for module_perms in tree.values():
                codes.extend(module_perms.keys())
            return codes
        except Exception as e:
            logger.warning("Failed to read permission cache from Redis: %s", e)
            return []

    def getPermissionList(self, login_id: str, login_type: str) -> list[str]:
        """Return the user's permission codes."""
        from plugins.plugin_sys.role.models import RelRolePermission
        from plugins.plugin_sys.user.models import RelUserPermission

        db = SessionLocal()
        try:
            role_ids = self._get_role_ids(login_id)

            if self._is_super_admin(role_ids):
                return self._get_all_permissions_from_redis()

            permission_codes = set()

            if login_type in (LoginTypeEnum.BUSINESS, LoginTypeEnum.CONSUMER):
                if role_ids:
                    rows = db.query(RelRolePermission.permission_code).filter(
                        RelRolePermission.role_id.in_(role_ids)
                    ).all()
                    permission_codes.update(r[0] for r in rows)

                rows = db.query(RelUserPermission.permission_code).filter(
                    RelUserPermission.user_id == login_id
                ).all()
                permission_codes.update(r[0] for r in rows)

            return list(permission_codes)
        finally:
            db.close()

    def getRoleList(self, login_id: str, login_type: str) -> list[str]:
        """Return role codes for a given login ID."""
        role_ids = self._get_role_ids(login_id)
        if not role_ids:
            return []

        roles = self._get_roles_by_ids(role_ids)
        return [r.code for r in roles]

    def getPermissionScopeMap(self, login_id: str, login_type: str) -> dict:
        """Return permission-to-scope info map."""
        from plugins.plugin_sys.user.models import RelUserPermission, RelUserRole
        from plugins.plugin_sys.role.models import RelRolePermission

        if login_type not in (LoginTypeEnum.BUSINESS, LoginTypeEnum.CONSUMER):
            return {}

        db = SessionLocal()
        try:
            role_ids = self._get_role_ids(login_id)

            if role_ids:
                roles = self._get_roles_by_ids(role_ids)
                for role in roles:
                    if role.code == SUPER_ADMIN_CODE:
                        all_codes = self._get_all_permissions_from_redis()
                        return {
                            code: {
                                "group_scope": DataScopeEnum.ALL,
                                "org_scope": DataScopeEnum.ALL,
                                "custom_group_ids": [],
                                "custom_org_ids": [],
                            }
                            for code in all_codes
                        }

            perm_scope = {}

            if role_ids:
                rows = db.query(
                    RelRolePermission.permission_code,
                    RelRolePermission.scope,
                    RelRolePermission.custom_scope_group_ids,
                    RelRolePermission.custom_scope_org_ids,
                ).filter(RelRolePermission.role_id.in_(role_ids)).all()
                self._merge_scope(perm_scope, PermissionPathEnum.USER_ROLE, rows)

            rows = db.query(
                RelUserPermission.permission_code,
                RelUserPermission.scope,
                RelUserPermission.custom_scope_group_ids,
                RelUserPermission.custom_scope_org_ids,
            ).filter(RelUserPermission.user_id == login_id).all()
            self._merge_scope(perm_scope, PermissionPathEnum.DIRECT, rows)

            return {
                k: {
                    "group_scope": v["group_scope"],
                    "org_scope": v["org_scope"],
                    "custom_group_ids": v.get("custom_group_ids", []),
                    "custom_org_ids": v.get("custom_org_ids", []),
                }
                for k, v in perm_scope.items()
            }
        finally:
            db.close()

    @staticmethod
    def _is_group_scope(scope: str) -> bool:
        return scope in (
            DataScopeEnum.GROUP, DataScopeEnum.GROUP_AND_BELOW,
            DataScopeEnum.CUSTOM_GROUP,
        )

    @staticmethod
    def _is_org_scope(scope: str) -> bool:
        return scope in (
            DataScopeEnum.ORG, DataScopeEnum.ORG_AND_BELOW,
            DataScopeEnum.CUSTOM_ORG,
        )

    def _merge_scope(self, result: dict, priority: str, rows: list):
        for code, scope, cgids_raw, cogids_raw in rows:
            scope = scope or DataScopeEnum.ALL
            cgids = json.loads(cgids_raw) if cgids_raw else []
            cogids = json.loads(cogids_raw) if cogids_raw else []

            is_group = self._is_group_scope(scope)
            is_org = self._is_org_scope(scope)
            is_all = scope == DataScopeEnum.ALL
            is_self = scope == DataScopeEnum.SELF

            if code not in result:
                result[code] = {
                    "group_scope": scope if (is_all or is_self or is_group) else None,
                    "org_scope": scope if (is_all or is_self or is_org) else None,
                    "custom_group_ids": cgids,
                    "custom_org_ids": cogids,
                    "priority": priority,
                }
            else:
                cur = result[code]
                if priority < cur["priority"]:
                    cur["group_scope"] = scope if (is_all or is_self or is_group) else None
                    cur["org_scope"] = scope if (is_all or is_self or is_org) else None
                    cur["custom_group_ids"] = cgids
                    cur["custom_org_ids"] = cogids
                    cur["priority"] = priority
                elif priority == cur["priority"]:
                    if is_group or is_all or is_self:
                        self._merge_dimension(cur, "group_scope", "custom_group_ids", scope, cgids)
                    if is_org or is_all or is_self:
                        self._merge_dimension(cur, "org_scope", "custom_org_ids", scope, cogids)

    @staticmethod
    def _merge_dimension(cur: dict, scope_key: str, ids_key: str, new_scope: str, new_ids: list):
        if cur_scope := cur.get(scope_key):
            if cur_scope == DataScopeEnum.SELF:
                return
            if new_scope == DataScopeEnum.SELF:
                cur[scope_key] = DataScopeEnum.SELF
                cur[ids_key] = []
                return
            restricted = DataScopeEnum.most_restrictive(cur_scope, new_scope)
            cur[scope_key] = restricted
            if restricted in (DataScopeEnum.CUSTOM_GROUP, DataScopeEnum.CUSTOM_ORG):
                merged = set(cur.get(ids_key, [])) | set(new_ids)
                cur[ids_key] = list(merged)
        else:
            cur[scope_key] = new_scope
            cur[ids_key] = new_ids
