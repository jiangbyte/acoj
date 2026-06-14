from fastapi import Request
from starlette.types import ASGIApp, Scope, Receive, Send

from sdk.config.settings import settings


class AuthMiddleware:
    """项目级鉴权中间件。

    这里只负责放行公共路径和静态路径，不再做 realm 猜测和自动登录态绑定。
    认证与鉴权统一交给显式 realm 的装饰器或业务代码处理。
    """

    STATIC_PATHS = [
        "/favicon.ico",
        "/docs",
        "/redoc",
        "/openapi.json",
        "/v3/api-docs",
    ]

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

        request = Request(scope, receive)
        if self._is_public_path(path):
            await self.app(scope, receive, send)
            return

        await self.app(scope, receive, send)

    def _is_static_path(self, path: str) -> bool:
        for static_path in self.STATIC_PATHS:
            if path == static_path or path.startswith(static_path):
                return True
        return False

    def _is_public_path(self, path: str) -> bool:
        for public_path in settings.auth.public_paths:
            if path == public_path or path.startswith(public_path):
                return True
        return False
