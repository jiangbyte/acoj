from typing import List, Optional, Union
from sqlalchemy import select, func

from core.db.mysql import SessionLocal
from core.enums import LoginTypeEnum, DataScopeEnum


def _soft_delete_filter(query, model):
    """Dynamically apply soft-delete filter using configured field/value.

    Reads from DB__SOFT_DELETE_* settings so it adapts to any field name
    or value convention. Skips models that don't have the configured field
    (e.g. rel_* junction tables without is_deleted).
    """
    from config.settings import settings as _settings
    if not _settings.db.soft_delete_enabled:
        return query
    field_name = _settings.db.soft_delete_field
    if hasattr(model, field_name):
        return query.where(getattr(model, field_name) == _settings.db.soft_delete_value_not_deleted)
    return query


class HeiPermissionInterface:
    """
    Runtime permission loader. Queries DB at request time.
    Resolves permissions from three paths:
      1. User → Role → RolePermission (permission_code)
      2. User → UserPermission (permission_code, direct grant)
      3. User → Org → OrgRole → RolePermission (permission_code)
    Scope resolution: higher path priority (lower P value) wins.
    Same priority: most restrictive scope wins within each dimension (group/org).
    """

    def _get_role_ids(self, db, login_id: str) -> List[str]:
        from modules.sys.user.models import RelUserRole

        role_ids = set()
        query = select(RelUserRole.role_id).where(RelUserRole.user_id == login_id)
        query = _soft_delete_filter(query, RelUserRole)
        rows = db.scalars(query).all()
        role_ids.update(rows)

        from modules.sys.org.models import RelOrgRole
        from modules.sys.user.models import SysUser
        user = db.get(SysUser, login_id)
        if user and user.org_id:
            query = select(RelOrgRole.role_id).where(RelOrgRole.org_id == user.org_id)
            query = _soft_delete_filter(query, RelOrgRole)
            rows = db.scalars(query).all()
            role_ids.update(rows)

        return list(role_ids)

    @staticmethod
    def _is_group_scope(scope: str) -> bool:
        return scope in (DataScopeEnum.GROUP, DataScopeEnum.GROUP_AND_BELOW, DataScopeEnum.CUSTOM_GROUP)

    @staticmethod
    def _is_org_scope(scope: str) -> bool:
        return scope in (DataScopeEnum.ORG, DataScopeEnum.ORG_AND_BELOW, DataScopeEnum.CUSTOM_ORG)

    def _merge_dimension(self, cur: dict, scope_key: str, ids_key: str, new_scope: str, new_ids: list):
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

    def _merge_scope(self, result: dict, priority: str, rows: list):
        import json
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
                    "custom_group_ids": cgids, "custom_org_ids": cogids,
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

    async def getPermissionList(self, login_id: Union[str, int], login_type: str) -> List[str]:
        from modules.sys.role.models import RelRolePermission
        from modules.sys.user.models import RelUserPermission

        db = SessionLocal()
        try:
            login_id = str(login_id)
            role_ids = self._get_role_ids(db, login_id)
            permission_codes = set()

            if login_type == LoginTypeEnum.LOGIN or login_type == LoginTypeEnum.CLIENT:
                if role_ids:
                    query = select(RelRolePermission.permission_code).where(RelRolePermission.role_id.in_(role_ids))
                    query = _soft_delete_filter(query, RelRolePermission)
                    rows = db.scalars(query).all()
                    permission_codes.update(rows)

                query = select(RelUserPermission.permission_code).where(RelUserPermission.user_id == login_id)
                query = _soft_delete_filter(query, RelUserPermission)
                rows = db.scalars(query).all()
                permission_codes.update(rows)

            return list(permission_codes)
        finally:
            db.close()

    async def getRoleList(self, login_id: Union[str, int], login_type: str) -> List[str]:
        from modules.sys.role.models import SysRole
        from modules.sys.user.models import RelUserRole

        db = SessionLocal()
        try:
            login_id = str(login_id)
            query = select(SysRole.code).join(RelUserRole, SysRole.id == RelUserRole.role_id).where(
                RelUserRole.user_id == login_id,
            )
            query = _soft_delete_filter(query, RelUserRole)
            query = _soft_delete_filter(query, SysRole)
            return list(db.scalars(query).all())
        finally:
            db.close()

    async def getPermissionScopeMap(self, login_id: Union[str, int], login_type: str) -> dict:
        import json
        from core.enums import PermissionPathEnum
        from modules.sys.user.models import RelUserRole, RelUserPermission
        from modules.sys.role.models import RelRolePermission
        from modules.sys.org.models import RelOrgRole
        from modules.sys.user.models import SysUser

        db = SessionLocal()
        try:
            login_id = str(login_id)
            if login_type != LoginTypeEnum.LOGIN and login_type != LoginTypeEnum.CLIENT:
                return {}

            perm_scope = {}

            # Path 1 (P1): User → Role → Permission (ral_role_permission.permission_code)
            query = select(RelUserRole.role_id).where(RelUserRole.user_id == login_id)
            query = _soft_delete_filter(query, RelUserRole)
            role_ids = db.scalars(query).all()
            if role_ids:
                query = select(
                    RelRolePermission.permission_code,
                    RelRolePermission.scope,
                    RelRolePermission.custom_scope_group_ids,
                    RelRolePermission.custom_scope_org_ids,
                ).where(RelRolePermission.role_id.in_(role_ids))
                query = _soft_delete_filter(query, RelRolePermission)
                rows = db.execute(query).all()
                self._merge_scope(perm_scope, PermissionPathEnum.USER_ROLE, rows)

            # Path 2 (P0): User → Direct Permission (ral_user_permission.permission_code)
            query = select(
                RelUserPermission.permission_code,
                RelUserPermission.scope,
                RelUserPermission.custom_scope_group_ids,
                RelUserPermission.custom_scope_org_ids,
            ).where(RelUserPermission.user_id == login_id)
            query = _soft_delete_filter(query, RelUserPermission)
            rows = db.execute(query).all()
            self._merge_scope(perm_scope, PermissionPathEnum.DIRECT, rows)

            # Path 4 (P3): User → Org → Role → Permission
            if login_type == LoginTypeEnum.LOGIN:
                user = db.get(SysUser, login_id)
                if user and user.org_id:
                    query = select(
                        RelRolePermission.permission_code,
                        func.coalesce(RelOrgRole.scope, RelRolePermission.scope),
                        func.coalesce(RelOrgRole.custom_scope_group_ids, RelRolePermission.custom_scope_group_ids),
                        func.coalesce(RelOrgRole.custom_scope_org_ids, RelRolePermission.custom_scope_org_ids),
                    ).join(RelRolePermission, RelRolePermission.role_id == RelOrgRole.role_id).where(
                        RelOrgRole.org_id == user.org_id,
                    )
                    query = _soft_delete_filter(query, RelOrgRole)
                    query = _soft_delete_filter(query, RelRolePermission)
                    rows = db.execute(query).all()
                    self._merge_scope(perm_scope, PermissionPathEnum.ORG_ROLE, rows)

            return {k: {
                "group_scope": v["group_scope"],
                "org_scope": v["org_scope"],
                "custom_group_ids": v.get("custom_group_ids", []),
                "custom_org_ids": v.get("custom_org_ids", []),
            } for k, v in perm_scope.items()}
        finally:
            db.close()
