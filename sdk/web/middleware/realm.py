from __future__ import annotations

import fnmatch

from fastapi import HTTPException, Request
from fastapi.responses import JSONResponse
from micosauth.adapters.fastapi.context import bind_request_realm, get_request_guard
from starlette.middleware.base import BaseHTTPMiddleware

from sdk.auth import BUSINESS_REALM_ID, CONSUMER_REALM_ID
from sdk.config.settings import settings
from sdk.web.result import failure


class RealmRoutingMiddleware(BaseHTTPMiddleware):
    """Bind a default micosauth realm to each request path.

    This middleware sets the default realm from the request path and performs
    login checks for non-public HTTP endpoints. Permission checks stay at the
    API layer.
    """

    async def dispatch(self, request: Request, call_next):
        path = request.url.path
        if not getattr(request.state, "micos_default_realm_id", None):
            bind_request_realm(request, self._resolve_realm_id(path))
        if self._should_skip_auth(request):
            return await call_next(request)
        try:
            await get_request_guard(request).require_login()
        except HTTPException as exc:
            return JSONResponse(
                status_code=exc.status_code,
                content=failure(message=str(exc.detail), code=exc.status_code),
            )
        return await call_next(request)

    def _resolve_realm_id(self, path: str) -> str:
        for pattern in settings.auth.consumer_realm_patterns:
            if self._match_path(path, pattern):
                return CONSUMER_REALM_ID
        for pattern in settings.auth.business_realm_patterns:
            if self._match_path(path, pattern):
                return BUSINESS_REALM_ID
        return BUSINESS_REALM_ID

    def _should_skip_auth(self, request: Request) -> bool:
        if request.method.upper() == "OPTIONS":
            return True
        path = request.url.path
        for public_path in settings.auth.public_paths:
            if path == public_path or path.startswith(public_path):
                return True
        return False

    def _match_path(self, path: str, pattern: str) -> bool:
        value = str(pattern or "").strip()
        if not value:
            return False
        if "*" in value or "?" in value:
            return fnmatch.fnmatch(path, value)
        return path == value or path.startswith(value)
