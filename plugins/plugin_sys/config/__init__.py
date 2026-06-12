from .api.v1 import router

from sdk.kernel.registry import register_router
register_router(router)

__all__ = ["router"]
