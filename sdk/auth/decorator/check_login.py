from __future__ import annotations

from sdk.auth.realm import resolve_realm

from ._support import call_maybe_awaitable, bind_guard_metadata, ensure_login, get_request, wrap_guard


def CheckLogin(func=None, *, realm_id: str | None = None):
    def decorator(target):
        bind_guard_metadata(target, "login", [], None, realm_id or "")

        async def handler(fn, *args, **kwargs):
            request = get_request(*args, **kwargs)
            realm = resolve_realm(realm_id, request, guard_name="CheckLogin")
            await ensure_login(request, realm.id)
            return await call_maybe_awaitable(fn, *args, **kwargs)

        wrapper = wrap_guard(target, handler)
        bind_guard_metadata(wrapper, "login", [], None, realm_id or "")
        return wrapper

    if func is not None:
        return decorator(func)
    return decorator


def check_login(func=None, *, realm_id: str | None = None):
    return CheckLogin(func, realm_id=realm_id)
