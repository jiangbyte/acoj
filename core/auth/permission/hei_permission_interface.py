from typing import List, Optional, Union
from sqlalchemy import select, func

from core.db.mysql import SessionLocal
from core.enums import SoftDeleteEnum, LoginTypeEnum, DataScopeEnum


class HeiPermissionInterface:
    """
    Runtime permission loader. Queries DB at request time.
    Resolves permissions from three paths:
      1. User → Role → RolePermission → Permission
      2. User → UserPermission → Permission (direct grant)
      3. User → Org → OrgRole → RolePermission → Permission
    Scope resolution: higher path priority (lower P value) wins.
    Same priority: most restrictive scope wins within each dimension (group/org).
    """

    def _get_role_ids(self, db, login_id: str) -> List[str]:
        """Collect all role IDs: direct + via org delegation."""
        from modules.sys.user.models import RalUserRole

        role_ids = set()

        # Direct user→role
        rows = db.scalars(
            select(RalUserRole.role_id).where(
                RalUserRole.user_id == login_id,
                RalUserRole.is_deleted == SoftDeleteEnum.NO
            )
        ).all()
        role_ids.update(rows)

        # User→org→role — only applicable for B-end users (sys_user)
        from modules.sys.org.models import RalOrgRole
        from modules.sys.user.models import SysUser
        user = db.get(SysUser, login_id)
        if user and user.org_id:
            rows = db.scalars(
                select(RalOrgRole.role_id).where(
                    RalOrgRole.org_id == user.org_id,
                    RalOrgRole.is_deleted == SoftDeleteEnum.NO
                )
            ).all()
            role_ids.update(rows)

        return list(role_ids)

    @staticmethod
    def _is_group_scope(scope: str) -> bool:
        return scope in (DataScopeEnum.GROUP, DataScopeEnum.GROUP_AND_BELOW, DataScopeEnum.CUSTOM_GROUP)

    @staticmethod
    def _is_org_scope(scope: str) -> bool:
        return scope in (DataScopeEnum.ORG, DataScopeEnum.ORG_AND_BELOW, DataScopeEnum.CUSTOM_ORG)

    def _merge_dimension(self, cur: dict, scope_key: str, ids_key: str, new_scope: str, new_ids: list):
        """Merge a single dimension (group or org) at same priority."""
        cur_scope = cur.get(scope_key)
        if cur_scope == DataScopeEnum.SELF:
            return
        if cur_scope is None:
            cur[scope_key] = new_scope
            cur[ids_key] = new_ids
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

    def _merge_scope(self, result: dict, priority: str, rows: list):
        """
        Merge permission scopes with path priority.
        Higher priority (lower P value) overrides lower priority.
        Same priority: most restrictive scope wins within each dimension.
        Group-dimension and org-dimension scopes are tracked independently,
        so a permission can have both a group scope and an org scope.
        rows: list of (code, scope, custom_scope_group_ids, custom_scope_org_ids) tuples
        """
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
                entry = {
                    "group_scope": scope if (is_all or is_self or is_group) else None,
                    "org_scope": scope if (is_all or is_self or is_org) else None,
                    "custom_group_ids": cgids, "custom_org_ids": cogids,
                    "priority": priority,
                }
                result[code] = entry
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
        from modules.sys.role.models import RalRolePermission
        from modules.sys.permission.models import SysPermission
        from modules.sys.user.models import RalUserPermission

        db = SessionLocal()
        try:
            login_id = str(login_id)
            role_ids = self._get_role_ids(db, login_id)
            permission_codes = set()

            if login_type == LoginTypeEnum.LOGIN or login_type == LoginTypeEnum.CLIENT:
                # Path: Role → Permission (covers direct roles and org-delegated roles)
                if role_ids:
                    rows = db.scalars(
                        select(SysPermission.code)
                        .join(RalRolePermission, SysPermission.id == RalRolePermission.permission_id)
                        .where(
                            RalRolePermission.role_id.in_(role_ids),
                            RalRolePermission.is_deleted == SoftDeleteEnum.NO,
                            SysPermission.is_deleted == SoftDeleteEnum.NO
                        )
                        .distinct()
                    ).all()
                    permission_codes.update(rows)

                # Path 3: Direct user→permission
                import logging as _lg
                from sqlalchemy import text as _t
                _raw = db.execute(_t("SELECT id, user_id, permission_id, is_deleted FROM ral_user_permission WHERE user_id = :uid"), {"uid": login_id}).fetchall()
                _lg.getLogger(__name__).warning(f"[DBG] ral_user_permission for {login_id}: {_raw}")
                rows = db.scalars(
                    select(SysPermission.code)
                    .join(RalUserPermission, SysPermission.id == RalUserPermission.permission_id)
                    .where(
                        RalUserPermission.user_id == login_id,
                        RalUserPermission.is_deleted == SoftDeleteEnum.NO,
                        SysPermission.is_deleted == SoftDeleteEnum.NO
                    )
                    .distinct()
                ).all()
                _lg.getLogger(__name__).warning(f"[DBG] Path3 result for {login_id}: {rows}")
                permission_codes.update(rows)

            result = list(permission_codes)
            import logging as _lg2
            _lg2.getLogger(__name__).warning(f"[DBG] getPermissionList result for {login_id} ({login_type}): {result}")
            return result
        finally:
            db.close()

    async def getRoleList(self, login_id: Union[str, int], login_type: str) -> List[str]:
        from modules.sys.role.models import SysRole
        from modules.sys.user.models import RalUserRole

        db = SessionLocal()
        try:
            login_id = str(login_id)
            return list(
                db.scalars(
                    select(SysRole.code)
                    .join(RalUserRole, SysRole.id == RalUserRole.role_id)
                    .where(
                        RalUserRole.user_id == login_id,
                        RalUserRole.is_deleted == SoftDeleteEnum.NO,
                        SysRole.is_deleted == SoftDeleteEnum.NO
                    )
                ).all()
            )
        finally:
            db.close()

    async def getPermissionScopeMap(self, login_id: Union[str, int], login_type: str) -> dict:
        """
        Returns {permission_code: {"group_scope": Optional[str], "org_scope": Optional[str],
                                    "custom_group_ids": list, "custom_org_ids": list}}

        Each permission may come from multiple paths. Effective scope is determined by
        path priority (lower P value wins). Same priority: most restrictive scope wins
        within each dimension (group vs org). Group and org scopes are tracked independently,
        so a permission can have both a group-scope and an org-scope simultaneously.

        Scope sources per path (all from relationship tables):
          - P0 Direct grant path: ral_user_permission.scope (+ custom_scope_group_ids, custom_scope_org_ids)
          - P1 Role→Permission path: ral_role_permission.scope (+ custom_scope_group_ids, custom_scope_org_ids)
          - P2 Org→Role→Permission path: ral_org_role.scope (+ custom_scope_group_ids, custom_scope_org_ids)
        """
        import json
        from core.enums import PermissionPathEnum
        from modules.sys.user.models import RalUserRole, RalUserPermission
        from modules.sys.role.models import RalRolePermission
        from modules.sys.org.models import RalOrgRole
        from modules.sys.user.models import SysUser
        from modules.sys.permission.models import SysPermission

        db = SessionLocal()
        try:
            login_id = str(login_id)
            if login_type != LoginTypeEnum.LOGIN and login_type != LoginTypeEnum.CLIENT:
                return {}

            perm_scope = {}

            # Path 1 (P1): User → Role → Permission (includes scope from ral_role_permission)
            role_ids = db.scalars(
                select(RalUserRole.role_id).where(
                    RalUserRole.user_id == login_id,
                    RalUserRole.is_deleted == SoftDeleteEnum.NO
                )
            ).all()
            if role_ids:
                rows = db.execute(
                    select(SysPermission.code, RalRolePermission.scope, RalRolePermission.custom_scope_group_ids, RalRolePermission.custom_scope_org_ids)
                    .join(RalRolePermission, SysPermission.id == RalRolePermission.permission_id)
                    .where(
                        RalRolePermission.role_id.in_(role_ids),
                        RalRolePermission.is_deleted == SoftDeleteEnum.NO,
                        SysPermission.is_deleted == SoftDeleteEnum.NO
                    )
                    .distinct()
                ).all()
                self._merge_scope(perm_scope, PermissionPathEnum.USER_ROLE, rows)

            # Path 2 (P0): User → Direct Permission
            rows = db.execute(
                select(SysPermission.code, RalUserPermission.scope, RalUserPermission.custom_scope_group_ids, RalUserPermission.custom_scope_org_ids)
                .join(RalUserPermission, SysPermission.id == RalUserPermission.permission_id)
                .where(
                    RalUserPermission.user_id == login_id,
                    RalUserPermission.is_deleted == SoftDeleteEnum.NO,
                    SysPermission.is_deleted == SoftDeleteEnum.NO
                )
                .distinct()
            ).all()
            self._merge_scope(perm_scope, PermissionPathEnum.DIRECT, rows)

            # Path 4 (P3): User → Org → Role → Permission — B-end only (sys_user.org_id)
            if login_type == LoginTypeEnum.LOGIN:
                user = db.get(SysUser, login_id)
                if user and user.org_id:
                    rows = db.execute(
                        select(SysPermission.code,
                               func.coalesce(RalOrgRole.scope, RalRolePermission.scope),
                               func.coalesce(RalOrgRole.custom_scope_group_ids, RalRolePermission.custom_scope_group_ids),
                               func.coalesce(RalOrgRole.custom_scope_org_ids, RalRolePermission.custom_scope_org_ids))
                        .join(RalRolePermission, SysPermission.id == RalRolePermission.permission_id)
                        .join(RalOrgRole, RalOrgRole.role_id == RalRolePermission.role_id)
                        .where(
                            RalOrgRole.org_id == user.org_id,
                            RalOrgRole.is_deleted == SoftDeleteEnum.NO,
                            RalRolePermission.is_deleted == SoftDeleteEnum.NO,
                            SysPermission.is_deleted == SoftDeleteEnum.NO
                        )
                        .distinct()
                    ).all()
                    self._merge_scope(perm_scope, PermissionPathEnum.ORG_ROLE, rows)

            # Strip priority before returning
            return {k: {
                "group_scope": v["group_scope"],
                "org_scope": v["org_scope"],
                "custom_group_ids": v.get("custom_group_ids", []),
                "custom_org_ids": v.get("custom_org_ids", []),
            } for k, v in perm_scope.items()}
        finally:
            db.close()
