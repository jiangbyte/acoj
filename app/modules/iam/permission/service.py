import logging
from typing import TypedDict

from app.core.exceptions.business import BusinessError
from app.core.security.permission_registry import (
    get_permission_definition,
    list_permission_definitions,
)

logger = logging.getLogger(__name__)


class PermissionDefinition(TypedDict):
    permission_key: str
    module: str
    source: str
    methods: list[str]
    account_types: list[str]
    routes: list[dict[str, object]]


class PermissionService:
    async def resolve_permission_definitions(
        self,
        permission_keys: list[str],
    ) -> dict[str, PermissionDefinition]:
        definitions: dict[str, PermissionDefinition] = {}
        for permission_key in permission_keys:
            item = await get_permission_definition(permission_key)
            if not item:
                logger.info(
                    "Permission definition missing from registry",
                    extra={"permission_key": permission_key},
                )
                continue
            definitions[permission_key] = {
                "permission_key": item.permission_key,
                "module": item.module,
                "source": item.source,
                "methods": list(item.methods),
                "account_types": list(item.account_types),
                "routes": [
                    {
                        "path": route_ref.path,
                        "methods": list(route_ref.methods),
                        "account_types": list(route_ref.account_types),
                    }
                    for route_ref in item.routes
                ],
            }
        return definitions

    async def list_permission_registry(self) -> list[PermissionDefinition]:
        items = await list_permission_definitions()
        return [
            {
                "permission_key": item.permission_key,
                "module": item.module,
                "source": item.source,
                "methods": list(item.methods),
                "account_types": list(item.account_types),
                "routes": [
                    {
                        "path": route_ref.path,
                        "methods": list(route_ref.methods),
                        "account_types": list(route_ref.account_types),
                    }
                    for route_ref in item.routes
                ],
            }
            for item in items
        ]


async def ensure_registered_permission(permission_key: str) -> None:
    item = await get_permission_definition(permission_key)
    if item is None:
        raise BusinessError(f"Permission is not registered in Redis: {permission_key}")
