import json
import logging
from datetime import datetime
from functools import wraps

from fastapi import Request

from core.db.redis import get_client
from core.exception import BusinessException
from core.utils import get_client_ip

logger = logging.getLogger(__name__)

NO_REPEAT_PREFIX = "norepeat:"


def _get_request(*args, **kwargs):
    request = kwargs.get('request')
    if request:
        return request
    for arg in args:
        if isinstance(arg, Request):
            return arg
    return None


async def _get_current_user_id(request: Request) -> str:
    """Get the current authenticated user ID, if any."""
    try:
        from core.auth import HeiAuthTool
        uid = await HeiAuthTool.getLoginIdDefaultNull(request)
        return str(uid) if uid else ""
    except Exception:
        return ""


_EXCLUDE_PARAMS = frozenset({"request", "db", "file"})


def _params_hash(func_kwargs: dict) -> str:
    """Serialize relevant kwargs to a deterministic hash for comparison."""
    try:
        filtered = {}
        for k, v in func_kwargs.items():
            if k in _EXCLUDE_PARAMS or v is None:
                continue
            try:
                json.dumps(v)
                filtered[k] = v
            except (TypeError, ValueError):
                filtered[k] = str(v)
        params_str = json.dumps(filtered, sort_keys=True, ensure_ascii=False, default=str)
        return str(hash(params_str))
    except Exception:
        return ""


def no_repeat(interval: int = 5000):
    """
    Prevent duplicate submissions within a time window.

    If the same user/IP submits identical params to the same URL within
    ``interval`` milliseconds, a BusinessException is raised.

    Usage::

        @router.post("/api/v1/sys/xxx/create")
        @NoRepeat(interval=3000)
        @HeiCheckPermission("sys:xxx:create")
        async def handler(request: Request, ...):
            ...
    """
    def decorator(func):
        @wraps(func)
        async def wrapper(*args, **kwargs):
            request = _get_request(*args, **kwargs)
            if not request:
                return await func(*args, **kwargs)

            user_id = await _get_current_user_id(request)
            ip = get_client_ip(request)
            cache_key = f"{NO_REPEAT_PREFIX}{ip}:{user_id}:{request.url.path}"
            phash = _params_hash(kwargs)

            redis = get_client()
            if redis:
                cached = await redis.get(cache_key)
                if cached:
                    try:
                        data = json.loads(cached)
                        if data.get("hash") == phash:
                            elapsed = int(datetime.now().timestamp() * 1000) - data.get("time", 0)
                            if elapsed < interval:
                                remaining = max(1, (interval - elapsed) // 1000)
                                raise BusinessException(
                                    f"请求过于频繁，请{remaining}秒后再试"
                                )
                    except (json.JSONDecodeError, KeyError):
                        pass

                now_ms = int(datetime.now().timestamp() * 1000)
                await redis.setex(
                    cache_key,
                    3600,
                    json.dumps({"hash": phash, "time": now_ms}),
                )

            return await func(*args, **kwargs)
        return wrapper
    return decorator


# Uppercase alias for use as @NoRepeat(interval=3000)
NoRepeat = no_repeat
