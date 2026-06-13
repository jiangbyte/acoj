import json
import logging
from functools import wraps

from fastapi import Request

from sdk.auth.consts import NoRepeatPrefix
from sdk.auth.decorator._support import call_maybe_awaitable, get_request, params_hash
from sdk.auth.enums import RealmID
from sdk.auth.realm import infer_realm_id_from_path, realm_from_id
from sdk.infra.db.redis import get_client
from sdk.web.exception import BusinessException
from sdk.utils import get_client_ip

logger = logging.getLogger(__name__)


async def _get_current_user_id(request: Request) -> str:
    """Get the current authenticated user ID, if any."""
    try:
        realm_id = infer_realm_id_from_path(request.url.path) or RealmID.BUSINESS
        uid = await realm_from_id(realm_id).get_login_id(request)
        return str(uid) if uid else ""
    except Exception:
        return ""


_EXCLUDE_PARAMS = frozenset({"request", "db", "file", "background_tasks"})


async def _build_request_hash(request: Request, func_kwargs: dict) -> str:
    params: dict[str, object] = {}

    for key, values in request.query_params.multi_items():
        existing = params.get(key)
        if existing is None:
            params[key] = values
        elif isinstance(existing, list):
            existing.append(values)
        else:
            params[key] = [existing, values]

    if request.method != "GET":
        content_type = request.headers.get("content-type", "")
        if not content_type.startswith("multipart/"):
            body = await request.body()
            if body:
                params["_body"] = body.decode("utf-8", errors="ignore")

    for key, value in func_kwargs.items():
        if key in _EXCLUDE_PARAMS or value is None:
            continue
        if key in params:
            continue
        try:
            json.dumps(value)
            params[key] = value
        except (TypeError, ValueError):
            params[key] = str(value)

    return params_hash(params)


def no_repeat(interval: int = 5000):
    """
    Prevent duplicate submissions within a time window.

    If the same user/IP submits identical params to the same URL within
    ``interval`` milliseconds, a BusinessException is raised.

    Usage::

        @router.post("/api/v1/sys/xxx/create")
        @NoRepeat(interval=3000)
        @CheckPermission("sys:xxx:create")
        async def handler(request: Request, ...):
            ...
    """
    def decorator(func):
        @wraps(func)
        async def wrapper(*args, **kwargs):
            request = get_request(*args, **kwargs)
            if not request:
                return await call_maybe_awaitable(func, *args, **kwargs)

            user_id = await _get_current_user_id(request)
            ip = get_client_ip(request)
            phash = await _build_request_hash(request, kwargs)
            cache_key = f"{NoRepeatPrefix}{ip}:{user_id}:{request.url.path}:{phash}"

            redis = get_client()
            if redis:
                ttl_ms = interval if interval > 0 else 1000
                ttl_seconds = max(1, (ttl_ms + 999) // 1000)
                accepted = await redis.set(cache_key, "1", ex=ttl_seconds, nx=True)
                if not accepted:
                    raise BusinessException(f"请求过于频繁，请{ttl_seconds}秒后再试")

            return await call_maybe_awaitable(func, *args, **kwargs)
        return wrapper
    return decorator


# Uppercase alias for use as @NoRepeat(interval=3000)
NoRepeat = no_repeat
