from fastapi import Request
from starlette.responses import JSONResponse
from starlette.types import ASGIApp, Scope, Receive, Send

from sdk.auth.decorator import attach_login_context
from sdk.auth.realm import all_realms, infer_realm, is_public_path
from sdk.config.settings import settings
from sdk.web.result import failure


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

        if is_public_path(path, settings.auth.public_paths):
            await self._attach_optional_login(request)
            await self.app(scope, receive, send)
            return

        realm = infer_realm(path)
        if realm:
            if not await realm.is_login(request):
                resp = JSONResponse(
                    status_code=401,
                    content=failure(message="未授权/未登录", code=401),
                )
                await resp(scope, receive, send)
                return
            await attach_login_context(request, realm.id)
        else:
            await self._attach_optional_login(request)

        await self.app(scope, receive, send)

    def _is_static_path(self, path: str) -> bool:
        for static_path in self.STATIC_PATHS:
            if path == static_path or path.startswith(static_path):
                return True
        return False

    async def _attach_optional_login(self, request: Request) -> None:
        if getattr(request.state, "login_id", None):
            return
        for realm in all_realms():
            if await realm.is_login(request):
                await attach_login_context(request, realm.id)
                return
