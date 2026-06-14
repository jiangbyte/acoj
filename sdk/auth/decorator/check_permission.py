from __future__ import annotations

from fastapi import HTTPException, status

from sdk.auth.enums import CheckMode
from sdk.auth.realm import check_permissions, resolve_realm

from ._support import (
    call_maybe_awaitable,
    bind_guard_metadata,
    ensure_login,
    get_request,
    join_values,
    normalize_values,
    wrap_guard,
)


def CheckPermission(permission: str | list[str], mode: str = CheckMode.AND, realm_id: str | None = None):
    def decorator(target):
        permissions = normalize_values(permission)
        bind_guard_metadata(target, "permission", permissions, mode, realm_id or "")

        async def handler(fn, *args, **kwargs):
            request = get_request(*args, **kwargs)
            realm = resolve_realm(realm_id, request, guard_name="CheckPermission")
            await ensure_login(request, realm.id)

            if mode == CheckMode.OR:
                allowed = await check_permissions(realm, permissions, request, CheckMode.OR)
            else:
                allowed = await check_permissions(realm, permissions, request, CheckMode.AND)

            if not allowed:
                raise HTTPException(
                    status_code=status.HTTP_403_FORBIDDEN,
                    detail=f"缺少权限: {join_values(permissions)}",
                )

            return await call_maybe_awaitable(fn, *args, **kwargs)

        wrapper = wrap_guard(target, handler)
        bind_guard_metadata(wrapper, "permission", permissions, mode, realm_id or "")
        return wrapper

    return decorator


def check_permission(permission: str | list[str], mode: str = CheckMode.AND, realm_id: str | None = None):
    return CheckPermission(permission, mode, realm_id)
