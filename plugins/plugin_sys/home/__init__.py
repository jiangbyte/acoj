from .api import v1_router as router

from core.plugin.registry import register_router
register_router(router)

__all__ = ["router"]
