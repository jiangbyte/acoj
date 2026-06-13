from __future__ import annotations

import json
import logging
from typing import Any

from plugins.plugin_sys.shared import SUPER_ADMIN_CODE
from sdk.auth.consts import PermissionCacheKey
from sdk.auth.enums import DataScope, RealmID

logger = logging.getLogger(__name__)


class PermissionProviderProtocol:
    async def get_permission_list(self, login_id: str, realm_id: str) -> list[str]:
        raise NotImplementedError

    async def get_role_list(self, login_id: str, realm_id: str) -> list[str]:
        raise NotImplementedError

    async def get_permission_scope_map(self, login_id: str, realm_id: str) -> dict[str, Any]:
        raise NotImplementedError


_permission_provider: PermissionProviderProtocol | None = None


def register_permission_provider(provider: PermissionProviderProtocol) -> None:
    global _permission_provider
    _permission_provider = provider
    logger.info("[auth] PermissionProvider registered")


def get_permission_provider() -> PermissionProviderProtocol | None:
    return _permission_provider


class DatabasePermissionProvider:
    def __init__(self, session_factory, redis_client_getter=None):
        self._session_factory = session_factory
        self._redis_client_getter = redis_client_getter

    def _open_db(self):
        return self._session_factory()

    async def _all_permission_codes(self) -> list[str]:
        redis_client = self._redis_client_getter() if self._redis_client_getter else None
        if redis_client is None:
            from sdk.infra.db.redis import get_client

            redis_client = get_client()
        if not redis_client:
            return []
        data = await redis_client.get(PermissionCacheKey)
        if not data:
            return []
        tree = json.loads(data)
        result: list[str] = []
        for module_entries in tree.values():
            result.extend(module_entries.keys())
        return result

    async def get_permission_list(self, login_id: str, realm_id: str) -> list[str]:
        from sqlalchemy import select
        from plugins.plugin_sys.role.models import RelRolePermission
        from plugins.plugin_sys.user.models import RelUserPermission, RelUserRole

        db = self._open_db()
        try:
            role_rows = db.scalars(select(RelUserRole.role_id).where(RelUserRole.user_id == login_id)).all()
            roles = await self.get_role_list(login_id, realm_id)
            if SUPER_ADMIN_CODE in roles:
                return await self._all_permission_codes()

            permissions: set[str] = set()
            if role_rows:
                permissions.update(
                    db.scalars(
                        select(RelRolePermission.permission_code).where(RelRolePermission.role_id.in_(role_rows))
                    ).all()
                )
            permissions.update(
                db.scalars(
                    select(RelUserPermission.permission_code).where(RelUserPermission.user_id == login_id)
                ).all()
            )
            return list(permissions)
        finally:
            db.close()

    async def get_role_list(self, login_id: str, realm_id: str) -> list[str]:
        from sqlalchemy import select
        from plugins.plugin_sys.role.models import SysRole
        from plugins.plugin_sys.user.models import RelUserRole

        db = self._open_db()
        try:
            query = (
                select(SysRole.code)
                .join(RelUserRole, SysRole.id == RelUserRole.role_id)
                .where(RelUserRole.user_id == login_id)
            )
            return list(db.scalars(query).all())
        finally:
            db.close()

    async def get_permission_scope_map(self, login_id: str, realm_id: str) -> dict[str, Any]:
        from sqlalchemy import select
        from plugins.plugin_sys.role.models import RelRolePermission
        from plugins.plugin_sys.user.models import RelUserPermission, RelUserRole

        if realm_id not in (RealmID.BUSINESS, RealmID.CONSUMER):
            return {}

        roles = await self.get_role_list(login_id, realm_id)
        if SUPER_ADMIN_CODE in roles:
            return {
                code: {
                    "group_scope": DataScope.ALL,
                    "org_scope": DataScope.ALL,
                    "custom_group_ids": [],
                    "custom_org_ids": [],
                }
                for code in await self._all_permission_codes()
            }

        db = self._open_db()
        try:
            role_ids = db.scalars(select(RelUserRole.role_id).where(RelUserRole.user_id == login_id)).all()
            result: dict[str, dict[str, Any]] = {}
            if role_ids:
                rows = db.execute(
                    select(
                        RelRolePermission.permission_code,
                        RelRolePermission.scope,
                        RelRolePermission.custom_scope_group_ids,
                        RelRolePermission.custom_scope_org_ids,
                    ).where(RelRolePermission.role_id.in_(role_ids))
                ).all()
                self._merge_scope(result, rows)

            rows = db.execute(
                select(
                    RelUserPermission.permission_code,
                    RelUserPermission.scope,
                    RelUserPermission.custom_scope_group_ids,
                    RelUserPermission.custom_scope_org_ids,
                ).where(RelUserPermission.user_id == login_id)
            ).all()
            self._merge_scope(result, rows)

            return {
                key: {
                    "group_scope": value["group_scope"],
                    "org_scope": value["org_scope"],
                    "custom_group_ids": value.get("custom_group_ids", []),
                    "custom_org_ids": value.get("custom_org_ids", []),
                }
                for key, value in result.items()
            }
        finally:
            db.close()

    @staticmethod
    def _merge_scope(target: dict[str, dict[str, Any]], rows: list[tuple[Any, ...]]) -> None:
        for code, scope, group_ids_raw, org_ids_raw in rows:
            scope = scope or DataScope.ALL.value
            custom_group_ids = DatabasePermissionProvider._parse_csv(group_ids_raw)
            custom_org_ids = DatabasePermissionProvider._parse_csv(org_ids_raw)
            current = target.get(code)
            if current is None:
                target[code] = {
                    "group_scope": scope,
                    "org_scope": scope,
                    "custom_group_ids": custom_group_ids,
                    "custom_org_ids": custom_org_ids,
                }
                continue
            current["group_scope"] = DataScope.most_restrictive(current["group_scope"], scope)
            current["org_scope"] = DataScope.most_restrictive(current["org_scope"], scope)
            current["custom_group_ids"] = DatabasePermissionProvider._merge_unique(
                current.get("custom_group_ids", []),
                custom_group_ids,
            )
            current["custom_org_ids"] = DatabasePermissionProvider._merge_unique(
                current.get("custom_org_ids", []),
                custom_org_ids,
            )

    @staticmethod
    def _parse_csv(raw: str | None) -> list[str]:
        if not raw:
            return []
        try:
            value = json.loads(raw)
        except json.JSONDecodeError:
            return [raw]
        if isinstance(value, list):
            return [str(item) for item in value]
        if value is None:
            return []
        return [str(value)]

    @staticmethod
    def _merge_unique(left: list[str], right: list[str]) -> list[str]:
        seen: set[str] = set()
        result: list[str] = []
        for item in [*left, *right]:
            if item not in seen:
                seen.add(item)
                result.append(item)
        return result
