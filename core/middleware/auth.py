import re
from fastapi import Request
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.responses import JSONResponse

from core.auth import HeiAuthTool, HeiClientAuthTool
from core.result import failure


class AuthMiddleware(BaseHTTPMiddleware):
    STATIC_PATHS = [
        "/favicon.ico",
        "/docs",
        "/redoc",
        "/openapi.json",
        "/v3/api-docs",
    ]

    PUBLIC_B_PATTERN = re.compile(r"^/api/v\d+/public/b/")
    PUBLIC_C_PATTERN = re.compile(r"^/api/v\d+/public/c/")
    PRIVATE_C_PATTERN = re.compile(r"^/api/v\d+/c/")
    PRIVATE_B_PATTERN = re.compile(r"^/api/v\d+/b/")
    DEFAULT_B_PATTERN = re.compile(r"^/api/v\d+/(?!b/|c/|public/)[^/]+/")

    async def dispatch(self, request: Request, call_next):
        path = request.url.path

        if self._is_static_path(path):
            return await call_next(request)

        if request.method == "OPTIONS":
            return await call_next(request)

        if self.PUBLIC_B_PATTERN.match(path) or self.PUBLIC_C_PATTERN.match(path):
            return await call_next(request)

        if self.PRIVATE_C_PATTERN.match(path):
            if not await HeiClientAuthTool.isLogin(request):
                return JSONResponse(
                    status_code=200,
                    content=failure(message="Unauthorized", code=401)
                )
            return await call_next(request)

        if self.PRIVATE_B_PATTERN.match(path) or self.DEFAULT_B_PATTERN.match(path):
            if not await HeiAuthTool.isLogin(request):
                return JSONResponse(
                    status_code=200,
                    content=failure(message="Unauthorized", code=401)
                )
            return await call_next(request)

        return await call_next(request)

    def _is_static_path(self, path: str) -> bool:
        for static_path in self.STATIC_PATHS:
            if path == static_path or path.startswith(static_path):
                return True
        return False
