from __future__ import annotations

from sdk.auth.enums import RealmID

from ._support import call_maybe_awaitable, bind_guard_metadata, ensure_login, get_request, wrap_guard


def CheckLogin(func=None, *, realm_id: str = RealmID.BUSINESS):
    def decorator(target):
        bind_guard_metadata(target, "login", [], None, realm_id)

        async def handler(fn, *args, **kwargs):
            request = get_request(*args, **kwargs)
            await ensure_login(request, realm_id)
            return await call_maybe_awaitable(fn, *args, **kwargs)

        wrapper = wrap_guard(target, handler)
        bind_guard_metadata(wrapper, "login", [], None, realm_id)
        return wrapper

    if func is not None:
        return decorator(func)
    return decorator


def check_login(func=None, *, realm_id: str = RealmID.BUSINESS):
    return CheckLogin(func, realm_id=realm_id)
