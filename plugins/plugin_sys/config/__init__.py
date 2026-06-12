from . import migrate
from .api.v1 import router

from core.plugin.registry import register_router
register_router(router)

__all__ = ["router"]
