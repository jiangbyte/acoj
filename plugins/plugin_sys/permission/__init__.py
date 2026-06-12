from .service import PermissionService, get_permission_service
from .api import v1_router as router

from sdk.kernel.registry import register_router
register_router(router)

__all__ = ["PermissionService", "get_permission_service", "router"]
