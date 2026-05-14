from starlette.types import ASGIApp, Scope, Receive, Send

from core.utils.trace_utils import generate_trace_id, set_trace_id, clear_trace_id, TRACE_ID_HEADER


class TraceMiddleware:
    """ASGI middleware that manages trace ID lifecycle per request.

    Sets a trace ID at the start of each request and clears it on completion.
    If the incoming request has a ``traceId`` header (e.g. from an upstream
    service), that value is propagated; otherwise a new UUID is generated.
    """

    def __init__(self, app: ASGIApp):
        self.app = app

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if scope["type"] != "http":
            await self.app(scope, receive, send)
            return

        trace_id = ""
        for name, value in scope.get("headers", []):
            if name == TRACE_ID_HEADER.encode("latin-1"):
                trace_id = value.decode("latin-1")
                break

        if not trace_id:
            trace_id = generate_trace_id()

        set_trace_id(trace_id)

        try:
            await self.app(scope, receive, send)
        finally:
            clear_trace_id()
