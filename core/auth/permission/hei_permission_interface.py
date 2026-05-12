from typing import List, Optional, Union
from sqlalchemy import select, func

from core.db.mysql import SessionLocal
from core.enums import SoftDeleteEnum, LoginTypeEnum, DataScopeEnum


class HeiPermissionInterface:
    """
    Runtime permission loader. Queries DB at request time.
    Resolves permissions from four paths:
      1. User → Role → RolePermission → Permission
      2. User → Group → GroupRole → RolePermission → Permission
      3. User → UserPermission → Permission (direct grant)
      4. User → Org → OrgRole → RolePermission → Permission
    Scope resolution: higher path priority (lower P value) wins.
    Same priority: most restrictive scope wins.
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

    def _merge_scope(self, result: dict, priority: str, rows: list):
        """
        Merge permission scopes with path priority.
        Higher priority (lower P value) overrides lower priority.
        Same priority: most restrictive scope wins.
        rows: list of (code, scope, custom_scope_group_ids, custom_scope_org_ids) tuples
        """
        import json
        for code, scope, cgids_raw, cogids_raw in rows:
            scope = scope or DataScopeEnum.ALL
            cgids = json.loads(cgids_raw) if cgids_raw else []
            cogids = json.loads(cogids_raw) if cogids_raw else []
            if code not in result:
                result[code] = {"scope": scope, "custom_group_ids": cgids, "custom_org_ids": cogids, "priority": priority}
            else:
                cur = result[code]
                if priority < cur["priority"]:
                    result[code] = {"scope": scope, "custom_group_ids": cgids, "custom_org_ids": cogids, "priority": priority}
                elif priority == cur["priority"]:
                    restricted = DataScopeEnum.most_restrictive(cur["scope"], scope)
                    result[code]["scope"] = restricted
                    if restricted in (DataScopeEnum.CUSTOM_GROUP, DataScopeEnum.CUSTOM_ORG):
                        merged_group = set(cur.get("custom_group_ids", [])) | set(cgids)
                        result[code]["custom_group_ids"] = list(merged_group)
                        merged_org = set(cur.get("custom_org_ids", [])) | set(cogids)
                        result[code]["custom_org_ids"] = list(merged_org)

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
                # Path 1 & 2: Role → Permission (covers direct roles, group-delegated roles, org roles)
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
        Returns {permission_code: {"scope": str, "custom_group_ids": list}} for the user.

        Each permission may come from multiple paths. Effective scope is determined by
        path priority (lower P value wins). Same priority: most restrictive scope wins.
        Scope sources per path (all from relationship tables):
          - P0 Direct grant path: ral_user_permission.scope (+ custom_scope_group_ids)
          - P1 Role→Permission path: ral_role_permission.scope (+ custom_scope_group_ids)
          - P2 Group→Role→Permission path: ral_group_role.scope (+ custom_scope_group_ids)
          - P3 Org→Role→Permission path: ral_org_role.scope (+ custom_scope_group_ids)
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
            return {k: {"scope": v["scope"], "custom_group_ids": v.get("custom_group_ids", []), "custom_org_ids": v.get("custom_org_ids", [])}
                    for k, v in perm_scope.items()}
        finally:
            db.close()
