from __future__ import annotations

from fastapi import HTTPException, status

from sdk.auth.enums import CheckMode, RealmID
from sdk.auth.realm import check_permissions, realm_from_id

from ._support import call_maybe_awaitable, bind_guard_metadata, ensure_login, get_request, join_values, normalize_values, wrap_guard


def CheckPermission(permission: str | list[str], mode: str = CheckMode.AND, realm_id: str = RealmID.BUSINESS):
    def decorator(target):
        permissions = normalize_values(permission)
        bind_guard_metadata(target, "permission", permissions, mode, realm_id)

        async def handler(fn, *args, **kwargs):
            request = get_request(*args, **kwargs)
            await ensure_login(request, realm_id)
            realm = realm_from_id(realm_id)

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
        bind_guard_metadata(wrapper, "permission", permissions, mode, realm_id)
        return wrapper

    return decorator


def check_permission(permission: str | list[str], mode: str = CheckMode.AND, realm_id: str = RealmID.BUSINESS):
    return CheckPermission(permission, mode, realm_id)
