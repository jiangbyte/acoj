from .models import SysGroup
from .params import GroupVO, GroupPageParam
from .repository import GroupRepository
from .service import GroupService, get_group_service
from .api import v1_router as router

from sdk.kernel.registry import register_router
register_router(router)

__all__ = ["SysGroup", "GroupVO", "GroupPageParam", "GroupRepository", "GroupService", "get_group_service", "router"]
