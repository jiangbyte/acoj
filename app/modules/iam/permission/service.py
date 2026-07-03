from app.core.security.permission_registry import (
    ensure_registered_permission_key,
    ensure_registered_permission_keys,
    list_permission_resources,
)


class PermissionService:
    async def list_permission_resources(self) -> list[str]:
        resources = await list_permission_resources()
        return sorted(
            resource
            for resource in resources
            if not resource.startswith("/{")
            and not resource.startswith("/error")
            and "/api-docs" not in resource
            and "/swagger-resources" not in resource
        )


async def ensure_registered_permission(permission_key: str) -> None:
    await ensure_registered_permission_key(permission_key)


async def ensure_registered_permissions(permission_keys: list[str]) -> None:
    await ensure_registered_permission_keys(permission_keys)
