import re

from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request

from app.modules.sys.audit.service import OperationAuditService
from app.platform.db.session import get_session_factory

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
            async with get_session_factory()() as session:
                await OperationAuditService(session).record(
                    module="iam" if resource_type != "resources" else "resource",
                    resource_type=resource_type,
                    action=action,
                    summary=f"{request.method} {request.url.path}",
                    success=response.status_code < 400,
                    error_message=None if response.status_code < 400 else str(response.status_code),
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
