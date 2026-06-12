"""
Exception handling middleware — mirrors hei-gin's ``sdk/middleware/recovery.go``.

Provides:
* ``setup_exception_handlers(app)`` — register exception handlers (FastAPI style)
* ``SafeCall(fn)`` — panic-safe function execution (hei-gin style)
"""

from __future__ import annotations

import logging
import traceback
from functools import wraps
from typing import Any, Callable, Optional

from fastapi import Request, FastAPI, HTTPException
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError

from sdk.web.exception import BusinessException
from sdk.web.result import failure
from sdk.log import save_exception_log

logger = logging.getLogger(__name__)


# ═════════════════════════════════════════════════════════════════════
# FastAPI exception handlers
# ═════════════════════════════════════════════════════════════════════

def setup_exception_handlers(app: FastAPI):
    """Register global exception handlers on the FastAPI app.

    Mirrors hei-gin's ``middleware.Recovery()`` by ensuring all panics
    and exceptions return structured JSON responses.
    """

    @app.exception_handler(BusinessException)
    async def business_exception_handler(request: Request, exc: BusinessException):
        logger.warning("Business exception: %s", exc.message)
        if not getattr(request.state, '_exception_logged', False):
            await save_exception_log(request, exc, name=exc.message)
        return JSONResponse(
            status_code=200,
            content=failure(message=exc.message, code=exc.code),
        )

    @app.exception_handler(HTTPException)
    async def http_exception_handler(request: Request, exc: HTTPException):
        logger.warning("HTTP exception %d: %s", exc.status_code, exc.detail)
        if not getattr(request.state, '_exception_logged', False):
            await save_exception_log(request, exc, name=f"HTTP {exc.status_code}")
        return JSONResponse(
            status_code=exc.status_code,
            content=failure(message=exc.detail, code=exc.status_code),
        )

    @app.exception_handler(RequestValidationError)
    async def validation_exception_handler(request: Request, exc: RequestValidationError):
        errors = exc.errors()
        logger.error("Validation error for %s %s: %s", request.method, request.url.path, errors)
        if not getattr(request.state, '_exception_logged', False):
            await save_exception_log(request, exc, name="请求参数校验失败")
        message = errors[0].get("msg", "请求参数格式错误") if errors else "请求参数格式错误"
        return JSONResponse(
            status_code=200,
            content=failure(message=message, code=400),
        )

    @app.exception_handler(Exception)
    async def global_exception_handler(request: Request, exc: Exception):
        logger.error("Global exception: %s\n%s", exc, "".join(
            traceback.format_exception(type(exc), exc, exc.__traceback__)
        ))
        if not getattr(request.state, '_exception_logged', False):
            await save_exception_log(request, exc)
        return JSONResponse(
            status_code=200,
            content=failure(message="服务器内部错误", code=500),
        )


# ═════════════════════════════════════════════════════════════════════
# SafeCall  —  mirrors hei-gin middleware.SafeCall() + SafeCallCtx()
# ═════════════════════════════════════════════════════════════════════

class SafeCallError(Exception):
    """Wrapper for BusinessError panics caught by SafeCall."""
    def __init__(self, message: str, code: int = 500):
        self.code = code
        super().__init__(message)


def SafeCall(fn: Callable[[], Any]) -> Optional[Exception]:
    """Execute *fn* with panic recovery.

    ``BusinessException`` raised inside *fn* is caught and returned as
    a ``SafeCallError``.  All other exceptions propagate normally.

    Returns ``None`` if *fn* completes without error.

    Usage::

        err = SafeCall(lambda: risky_operation())
        if err:
            logger.warning("Operation failed: %s", err)

    Mirrors hei-gin's ``middleware.SafeCall(fn)``.
    """
    try:
        fn()
        return None
    except BusinessException as e:
        return SafeCallError(e.message, e.code)
    except Exception:
        raise


async def SafeCallAsync(fn: Callable[[], Any]) -> Optional[Exception]:
    """Async version of ``SafeCall``.

    Mirrors hei-gin's pattern for async-safe calls.
    """
    try:
        await fn()
        return None
    except BusinessException as e:
        return SafeCallError(e.message, e.code)
    except Exception:
        raise


def safe_call_decorator(func: Callable) -> Callable:
    """Decorator that wraps a function with SafeCall.

    Usage::

        @safe_call_decorator
        def my_handler():
            risky_operation()
    """
    @wraps(func)
    def wrapper(*args, **kwargs):
        err = SafeCall(lambda: func(*args, **kwargs))
        if err:
            logger.warning("safe_call caught: %s", err)
        return err
    return wrapper
