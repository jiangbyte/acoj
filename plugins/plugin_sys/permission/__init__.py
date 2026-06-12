from .service import PermissionService
from .api import v1_router as router

from sdk.kernel.registry import register_router
register_router(router)

__all__ = ["PermissionService", "router"]
