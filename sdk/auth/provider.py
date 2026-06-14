from __future__ import annotations

import json
from typing import Any, Callable

from micosauth.provider import EmptyMicosAccessProvider, MicosAccessProvider

from sdk.auth.consts import PermissionCacheKey
from sdk.auth.enums import DataScope


class PermissionProviderProtocol(MicosAccessProvider):
    async def get_acl(self, realm_id: str, login_id: str) -> dict[str, Any]:
        return {
            "permissions": await self.get_permissions(realm_id, login_id),
            "roles": await self.get_roles(realm_id, login_id),
            "data_scopes": await self.get_data_scopes(realm_id, login_id),
            "extra": await self.get_extra(realm_id, login_id),
        }


class EmptyPermissionProvider(EmptyMicosAccessProvider):
    pass


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

    async def get_roles(self, realm_id: str, login_id: str) -> list[str]:
        del realm_id
        async with self._session_factory() as db:
            _, role_codes = await self._load_user_role_data(db, login_id)
            return list(role_codes)

    async def get_permissions(self, realm_id: str, login_id: str) -> list[str]:
        del realm_id
        async with self._session_factory() as db:
            acl = await self._build_acl(db, login_id)
            return list(acl["permissions"])

    async def get_data_scopes(self, realm_id: str, login_id: str) -> list[str]:
        del realm_id
        async with self._session_factory() as db:
            acl = await self._build_acl(db, login_id)
            return list(acl["data_scopes"])

    async def get_extra(self, realm_id: str, login_id: str) -> dict[str, Any]:
        del realm_id, login_id
        return {}

    async def _all_permission_codes(self) -> list[str]:
        if self._permission_code_loader is not None:
            async with self._session_factory() as db:
                return [str(item) for item in self._permission_code_loader(db)]
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

    async def _load_user_role_data(self, db, login_id: str) -> tuple[list[Any], list[str]]:
        from sqlalchemy import select

        role_ids = list(
            (await db.scalars(
                select(self._user_role_model.role_id).where(self._user_role_model.user_id == login_id)
            )).all()
        )
        if not role_ids:
            return [], []
        role_codes = list(
            (await db.scalars(
                select(self._role_model.code).where(self._role_model.id.in_(role_ids))
            )).all()
        )
        return role_ids, role_codes

    async def _build_acl(self, db, login_id: str) -> dict[str, Any]:
        from sqlalchemy import select

        role_ids, role_codes = await self._load_user_role_data(db, login_id)
        if self._super_admin_code in role_codes:
            permissions = await self._all_permission_codes()
            return {
                "permissions": permissions,
                "roles": role_codes,
                "data_scopes": [DataScope.ALL.value],
            }

        permissions: set[str] = set()
        data_scopes: set[str] = set()
        if role_ids:
            role_permission_rows = (await db.execute(
                select(
                    self._role_permission_model.permission_code,
                    self._role_permission_model.scope,
                ).where(self._role_permission_model.role_id.in_(role_ids))
            )).all()
            permissions.update(str(row[0]) for row in role_permission_rows if row[0])
            data_scopes.update(str(row[1] or DataScope.ALL.value) for row in role_permission_rows)

        user_permission_rows = (await db.execute(
            select(
                self._user_permission_model.permission_code,
                self._user_permission_model.scope,
            ).where(self._user_permission_model.user_id == login_id)
        )).all()
        permissions.update(str(row[0]) for row in user_permission_rows if row[0])
        data_scopes.update(str(row[1] or DataScope.ALL.value) for row in user_permission_rows)

        return {
            "permissions": list(permissions),
            "roles": role_codes,
            "data_scopes": list(data_scopes),
        }
