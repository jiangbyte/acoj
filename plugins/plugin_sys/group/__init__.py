from .models import SysGroup
from .params import GroupVO, GroupPageParam
from .dao import GroupDao
from .service import GroupService
from .api import v1_router as router

from core.plugin.registry import register_router
register_router(router)

__all__ = ["SysGroup", "GroupVO", "GroupPageParam", "GroupDao", "GroupService", "router"]
