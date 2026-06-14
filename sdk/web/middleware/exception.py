"""
Exception handling middleware — mirrors hei-gin's ``sdk/middleware/recovery.go``.

Provides:
* ``setup_exception_handlers(app)`` — register exception handlers (FastAPI style)
"""

from __future__ import annotations

import logging
import traceback

from fastapi import Request, FastAPI, HTTPException
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError

from sdk.web.exception import BusinessException
from sdk.web.result import failure, http_status
from sdk.log import save_exception_log
from sdk.observability import inc_http_panic

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
            status_code=http_status(exc.code),
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
            status_code=http_status(400),
            content=failure(message=message, code=400),
        )

    @app.exception_handler(Exception)
    async def global_exception_handler(request: Request, exc: Exception):
        inc_http_panic()
        logger.error("Global exception: %s\n%s", exc, "".join(
            traceback.format_exception(type(exc), exc, exc.__traceback__)
        ))
        if not getattr(request.state, '_exception_logged', False):
            await save_exception_log(request, exc)
        return JSONResponse(
            status_code=http_status(500),
            content=failure(message="服务器内部错误", code=500),
        )
