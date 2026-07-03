import re

from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request

from app.deps.context import (
    account_id_ctx,
    account_type_ctx,
    client_ip_ctx,
    request_id_ctx,
    user_agent_ctx,
)
from app.modules.sys.audit.queue import OperationAuditEvent, operation_audit_queue

AUDIT_METHODS = {"POST", "PUT", "PATCH", "DELETE"}
AUDIT_PATH_RE = re.compile(
    r"^/api/v\d+/admin/sys/"
    r"(?P<resource>accounts|roles|groups|depts|positions|resources|resource-modules)(?P<action>/[^?]*)?"
)


class OperationAuditMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        response = await call_next(request)
        audit_info = _match_audit_target(request)
        if audit_info is not None:
            resource_type, action = audit_info
            operation_audit_queue.enqueue(
                OperationAuditEvent(
                    resource_type=resource_type,
                    action=action,
                    method=request.method,
                    path=request.url.path,
                    status_code=response.status_code,
                    account_id=account_id_ctx.get(),
                    account_type=account_type_ctx.get(),
                    request_id=request_id_ctx.get(),
                    ip=client_ip_ctx.get(),
                    user_agent=user_agent_ctx.get(),
                )
            )
        return response


def _match_audit_target(request: Request) -> tuple[str, str] | None:
    if request.method.upper() not in AUDIT_METHODS:
        return None
    match = AUDIT_PATH_RE.match(request.url.path)
    if not match:
        return None
    resource = match.group("resource")
    action_path = (match.group("action") or "").strip("/")
    action = action_path.split("/", 1)[0] if action_path else request.method.lower()
    return resource, action.replace("-", "_")
