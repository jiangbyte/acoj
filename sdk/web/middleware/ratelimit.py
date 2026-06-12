"""
Rate limiting middleware — mirrors hei-gin's ``sdk/middleware/ratelimit.go``.

Uses Redis INCR + EXPIRE with a Lua script for atomic distributed rate
limiting across instances.

Usage::

    from sdk.web.middleware import RateLimiter

    @router.get("/api/v1/sys/user/page")
    @RateLimiter("user:page", window=10, max_requests=30)
    async def page_handler(...): ...
"""

from __future__ import annotations

import logging
from functools import wraps
from typing import Optional

from fastapi import Request, HTTPException, status

from sdk.infra.db.redis import get_client

logger = logging.getLogger(__name__)

# Defaults matching hei-gin
DEFAULT_WINDOW = 10       # seconds
DEFAULT_MAX_REQUESTS = 30


def RateLimiter(
    endpoint_key: str,
    window: int = DEFAULT_WINDOW,
    max_requests: int = DEFAULT_MAX_REQUESTS,
):
    """Decorator that limits requests per user (or IP) per time window.

    Args:
        endpoint_key: Unique identifier for the rate limit scope.
        window: Time window in seconds.
        max_requests: Max requests allowed per window.

    Mirrors hei-gin's ``middleware.RateLimiter(endpointKey, window, maxRequests)``.
    """
    win = max(window, 1)
    max_req = max(max_requests, 1)

    def decorator(func):
        @wraps(func)
        async def wrapper(*args, **kwargs):
            # Extract request from args/kwargs
            request: Optional[Request] = None
            for arg in args:
                if isinstance(arg, Request):
                    request = arg
                    break
            if not request:
                for v in kwargs.values():
                    if isinstance(v, Request):
                        request = v
                        break

            if not request:
                return await func(*args, **kwargs)

            # User ID from auth context, fallback to client IP
            user_id = ""
            try:
                from sdk.auth import HeiAuthTool
                uid = await HeiAuthTool.getLoginIdDefaultNull(request)
                if uid:
                    user_id = str(uid)
            except Exception:
                pass

            if not user_id:
                user_id = request.client.host if request.client else "unknown"

            key = f"ratelimit:api:{endpoint_key}:{user_id}"
            redis = get_client()
            if redis is None:
                # Redis unavailable — allow through
                return await func(*args, **kwargs)

            # Atomic Lua script: INCR + EXPIRE on first creation
            script = """
                local key = KEYS[1]
                local window = tonumber(ARGV[1])
                local max = tonumber(ARGV[2])
                local current = redis.call("INCR", key)
                if current == 1 then
                    redis.call("EXPIRE", key, window)
                end
                return current
            """
            try:
                val = await redis.eval(script, 1, key, win, max_req)
                count = int(val) if val is not None else 0
                if count > max_req:
                    raise HTTPException(
                        status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                        detail="请求过于频繁，请稍后重试",
                    )
            except HTTPException:
                raise
            except Exception:
                logger.warning("[RateLimiter] Redis error, allowing request")
                # Redis error — allow through

            return await func(*args, **kwargs)

        return wrapper

    return decorator


__all__ = ["RateLimiter"]
