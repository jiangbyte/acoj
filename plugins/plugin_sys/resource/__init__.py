from .models import SysModule, SysResource
from .params import ModuleVO, ResourceVO, ModulePageParam, ResourcePageParam
from .repository import ModuleRepository, ResourceRepository
from .service import ModuleService, ResourceService
from .api import v1_router as router

from core.plugin.registry import register_router
register_router(router)

__all__ = [
    "SysModule", "SysResource",
    "ModuleVO", "ResourceVO", "ModulePageParam", "ResourcePageParam",
    "ModuleRepository", "ResourceRepository",
    "ModuleService", "ResourceService",
    "router"
]
