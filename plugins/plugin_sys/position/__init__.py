from .models import SysPosition
from .params import PositionVO, PositionPageParam
from .repository import PositionRepository
from .service import PositionService, get_position_service
from .api import v1_router as router

from sdk.kernel.registry import register_router
register_router(router)

__all__ = ["SysPosition", "PositionVO", "PositionPageParam", "PositionRepository", "PositionService", "get_position_service", "router"]
