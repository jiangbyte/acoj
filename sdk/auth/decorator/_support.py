from __future__ import annotations

import json
from functools import wraps
from typing import Any

from fastapi import HTTPException, Request, status

from sdk.auth.realm import realm_from_id


def get_request(*args, **kwargs) -> Request | None:
    request = kwargs.get("request")
    if isinstance(request, Request):
        return request
    for arg in args:
        if isinstance(arg, Request):
            return arg
    return None


async def attach_login_context(request: Request | None, realm_id: str) -> str:
    if request is None:
        return ""

    realm = realm_from_id(realm_id)
    login_id = await realm.get_login_id(request) or ""
    if not login_id:
        return ""

    request.state.login_id = login_id
    request.state.realm_id = realm.id
    request.state.login_realm = realm

    username = await realm.get_extra("username", request)
    if isinstance(username, str) and username:
        request.state.loginUser = username

    return str(login_id)


async def ensure_login(request: Request | None, realm_id: str) -> str:
    login_id = await attach_login_context(request, realm_id)
    if not login_id:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="未授权/未登录")
    return login_id


def bind_guard_metadata(target, name: str, values: list[str], mode: str | None, realm_id: str) -> None:
    setattr(target, f"_{name}_codes", values)
    if mode is not None:
        setattr(target, f"_{name}_mode", mode)
    setattr(target, f"_{name}_realm_id", realm_id)


def wrap_guard(target, handler):
    @wraps(target)
    async def wrapper(*args, **kwargs):
        return await handler(target, *args, **kwargs)

    return wrapper


def join_values(values: list[str]) -> str:
    return ",".join(str(item) for item in values)


def normalize_values(value: str | list[str] | tuple[str, ...]) -> list[str]:
    if isinstance(value, str):
        return [value]
    return [str(item) for item in value]


def params_hash(params: dict[str, Any]) -> str:
    encoded = json.dumps(params, sort_keys=True, ensure_ascii=False, default=str)
    return format(hash(encoded) & 0xFFFFFFFFFFFFFFFF, "x")
