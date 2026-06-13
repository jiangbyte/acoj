from __future__ import annotations

import time

from starlette.types import ASGIApp, Scope, Receive, Send, Message

from sdk.observability import dec_http_inflight, inc_http_inflight, observe_http_request


class MetricsMiddleware:
    def __init__(self, app: ASGIApp):
        self.app = app

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if scope["type"] != "http":
            await self.app(scope, receive, send)
            return

        start = time.perf_counter()
        status_code = 200
        inc_http_inflight()

        async def send_wrapper(message: Message) -> None:
            nonlocal status_code
            if message["type"] == "http.response.start":
                status_code = int(message["status"])
            await send(message)

        try:
            await self.app(scope, receive, send_wrapper)
        finally:
            dec_http_inflight()
            route = scope.get("route")
            route_path = getattr(route, "path", None) or scope.get("path", "")
            observe_http_request(
                scope.get("method", ""),
                route_path,
                status_code,
                time.perf_counter() - start,
            )
