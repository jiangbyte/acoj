from __future__ import annotations

from fastapi import HTTPException, status

from sdk.auth.enums import CheckMode, RealmID
from sdk.auth.realm import check_roles, realm_from_id

from ._support import bind_guard_metadata, ensure_login, get_request, join_values, normalize_values, wrap_guard


def CheckRole(role: str | list[str], mode: str = CheckMode.AND, realm_id: str = RealmID.BUSINESS):
    def decorator(target):
        roles = normalize_values(role)
        bind_guard_metadata(target, "role", roles, mode, realm_id)

        async def handler(fn, *args, **kwargs):
            request = get_request(*args, **kwargs)
            await ensure_login(request, realm_id)
            realm = realm_from_id(realm_id)

            if mode == CheckMode.OR:
                allowed = await check_roles(realm, roles, request, CheckMode.OR)
            else:
                allowed = await check_roles(realm, roles, request, CheckMode.AND)

            if not allowed:
                raise HTTPException(
                    status_code=status.HTTP_403_FORBIDDEN,
                    detail=f"缺少角色: {join_values(roles)}",
                )

            return await fn(*args, **kwargs)

        wrapper = wrap_guard(target, handler)
        bind_guard_metadata(wrapper, "role", roles, mode, realm_id)
        return wrapper

    return decorator


def check_role(role: str | list[str], mode: str = CheckMode.AND, realm_id: str = RealmID.BUSINESS):
    return CheckRole(role, mode, realm_id)
