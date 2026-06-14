from __future__ import annotations

import json
import logging
from typing import Any, Callable

from sdk.auth.consts import PermissionCacheKey
from sdk.auth.enums import DataScope

logger = logging.getLogger(__name__)


class PermissionProviderProtocol:
    async def get_acl(self, login_id: str) -> dict[str, Any]:
        return {
            "permissions": await self.get_permission_list(login_id),
            "roles": await self.get_role_list(login_id),
            "scope_map": await self.get_permission_scope_map(login_id),
        }

    async def get_permission_list(self, login_id: str) -> list[str]:
        raise NotImplementedError

    async def get_role_list(self, login_id: str) -> list[str]:
        raise NotImplementedError

    async def get_permission_scope_map(self, login_id: str) -> dict[str, Any]:
        raise NotImplementedError


class EmptyPermissionProvider(PermissionProviderProtocol):
    async def get_acl(self, login_id: str) -> dict[str, Any]:
        return {"permissions": [], "roles": [], "scope_map": {}}

    async def get_permission_list(self, login_id: str) -> list[str]:
        return []

    async def get_role_list(self, login_id: str) -> list[str]:
        return []

    async def get_permission_scope_map(self, login_id: str) -> dict[str, Any]:
        return {}


EMPTY_PERMISSION_PROVIDER = EmptyPermissionProvider()


class DatabasePermissionProvider(PermissionProviderProtocol):
    def __init__(
        self,
        session_factory,
        *,
        role_model,
        role_permission_model,
        user_role_model,
        user_permission_model,
        super_admin_code: str,
        redis_client_getter=None,
        permission_cache_key: str = PermissionCacheKey,
        permission_code_loader: Callable[[Any], list[str]] | None = None,
    ):
        self._session_factory = session_factory
        self._role_model = role_model
        self._role_permission_model = role_permission_model
        self._user_role_model = user_role_model
        self._user_permission_model = user_permission_model
        self._super_admin_code = super_admin_code
        self._redis_client_getter = redis_client_getter
        self._permission_cache_key = permission_cache_key
        self._permission_code_loader = permission_code_loader

    def _open_session(self):
        return self._session_factory()

    async def _all_permission_codes(self) -> list[str]:
        if self._permission_code_loader is not None:
            db = self._open_session()
            try:
                return [str(item) for item in self._permission_code_loader(db)]
            finally:
                db.close()
        redis_client = self._redis_client_getter() if self._redis_client_getter else None
        if redis_client is None:
            from sdk.infra.db.redis import get_client

            redis_client = get_client()
        if not redis_client:
            return []
        data = await redis_client.get(self._permission_cache_key)
        if not data:
            return []
        tree = json.loads(data)
        result: list[str] = []
        for module_entries in tree.values():
            result.extend(module_entries.keys())
        return result

    async def get_permission_list(self, login_id: str) -> list[str]:
        db = self._open_session()
        try:
            acl = await self._build_acl(db, login_id, include_scope=False)
            return list(acl["permissions"])
        finally:
            db.close()

    async def get_role_list(self, login_id: str) -> list[str]:
        db = self._open_session()
        try:
            _, roles = self._load_user_role_data(db, login_id)
            return roles
        finally:
            db.close()

    async def get_permission_scope_map(self, login_id: str) -> dict[str, Any]:
        db = self._open_session()
        try:
            acl = await self._build_acl(db, login_id, include_scope=True)
            return dict(acl["scope_map"])
        finally:
            db.close()

    async def get_acl(self, login_id: str) -> dict[str, Any]:
        db = self._open_session()
        try:
            return await self._build_acl(db, login_id, include_scope=True)
        finally:
            db.close()

    def _load_user_role_data(self, db, login_id: str) -> tuple[list[Any], list[str]]:
        from sqlalchemy import select

        role_ids = list(
            db.scalars(
                select(self._user_role_model.role_id).where(self._user_role_model.user_id == login_id)
            ).all()
        )
        if not role_ids:
            return [], []
        role_codes = list(
            db.scalars(
                select(self._role_model.code).where(self._role_model.id.in_(role_ids))
            ).all()
        )
        return role_ids, role_codes

    async def _build_acl(self, db, login_id: str, *, include_scope: bool) -> dict[str, Any]:
        from sqlalchemy import select

        role_ids, role_codes = self._load_user_role_data(db, login_id)
        if self._super_admin_code in role_codes:
            permissions = await self._all_permission_codes()
            scope_map = self._super_admin_scope_map(permissions) if include_scope else {}
            return {
                "permissions": permissions,
                "roles": role_codes,
                "scope_map": scope_map,
            }

        permissions: set[str] = set()
        scope_map: dict[str, dict[str, Any]] = {}
        if role_ids:
            role_permission_rows = db.execute(
                select(
                    self._role_permission_model.permission_code,
                    self._role_permission_model.scope,
                    self._role_permission_model.custom_scope_group_ids,
                    self._role_permission_model.custom_scope_org_ids,
                ).where(self._role_permission_model.role_id.in_(role_ids))
            ).all()
            permissions.update(row[0] for row in role_permission_rows)
            if include_scope:
                self._merge_scope(scope_map, role_permission_rows)

        user_permission_rows = db.execute(
            select(
                self._user_permission_model.permission_code,
                self._user_permission_model.scope,
                self._user_permission_model.custom_scope_group_ids,
                self._user_permission_model.custom_scope_org_ids,
            ).where(self._user_permission_model.user_id == login_id)
        ).all()
        permissions.update(row[0] for row in user_permission_rows)
        if include_scope:
            self._merge_scope(scope_map, user_permission_rows)

        return {
            "permissions": list(permissions),
            "roles": role_codes,
            "scope_map": scope_map,
        }

    @staticmethod
    def _super_admin_scope_map(permission_codes: list[str]) -> dict[str, dict[str, Any]]:
        return {
            code: {
                "group_scope": DataScope.ALL,
                "org_scope": DataScope.ALL,
                "custom_group_ids": [],
                "custom_org_ids": [],
            }
            for code in permission_codes
        }

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
