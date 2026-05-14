import re
from fastapi import Request
from starlette.responses import JSONResponse
from starlette.types import ASGIApp, Scope, Receive, Send

from core.auth import HeiAuthTool, HeiClientAuthTool
from core.result import failure


class AuthMiddleware:
    """
    Raw ASGI middleware for auth checking.
    Does NOT use BaseHTTPMiddleware to avoid body streaming issues with multipart file uploads.
    """

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

    def __init__(self, app: ASGIApp):
        self.app = app

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if scope["type"] != "http":
            await self.app(scope, receive, send)
            return

        path = scope.get("path", "")

        if self._is_static_path(path):
            await self.app(scope, receive, send)
            return

        if scope.get("method") == "OPTIONS":
            await self.app(scope, receive, send)
            return

        # Create request for auth check (only reads headers, does NOT consume body)
        request = Request(scope, receive)

        if self.PUBLIC_B_PATTERN.match(path) or self.PUBLIC_C_PATTERN.match(path):
            await self.app(scope, receive, send)
            return

        if self.PRIVATE_C_PATTERN.match(path):
            if not await HeiClientAuthTool.isLogin(request):
                resp = JSONResponse(
                    status_code=200,
                    content=failure(message="Unauthorized", code=401)
                )
                await resp(scope, receive, send)
                return
            await self.app(scope, receive, send)
            return

        if self.PRIVATE_B_PATTERN.match(path) or self.DEFAULT_B_PATTERN.match(path):
            if not await HeiAuthTool.isLogin(request):
                resp = JSONResponse(
                    status_code=200,
                    content=failure(message="Unauthorized", code=401)
                )
                await resp(scope, receive, send)
                return
            await self.app(scope, receive, send)
            return

        await self.app(scope, receive, send)

    def _is_static_path(self, path: str) -> bool:
        for static_path in self.STATIC_PATHS:
            if path == static_path or path.startswith(static_path):
                return True
        return False
