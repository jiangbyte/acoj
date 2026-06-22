import uuid

from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request

from app.deps.context import request_id_ctx, request_method_ctx, request_path_ctx, span_id_ctx, trace_id_ctx
from app.platform.observability.tracing import sync_trace_context


class TraceMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        request_id = request.headers.get("X-Request-Id", uuid.uuid4().hex)
        request_token = request_id_ctx.set(request_id)
        path_token = request_path_ctx.set(request.url.path)
        method_token = request_method_ctx.set(request.method)
        try:
            sync_trace_context()
            response = await call_next(request)
        finally:
            request_id_ctx.reset(request_token)
            request_path_ctx.reset(path_token)
            request_method_ctx.reset(method_token)
            trace_id_ctx.set(None)
            span_id_ctx.set(None)
        response.headers["X-Request-Id"] = request_id
        return response
