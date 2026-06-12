from .models import SysPosition
from .params import PositionVO, PositionPageParam
from .dao import PositionDao
from .service import PositionService
from . import migrate
from .api import v1_router as router

from core.plugin.registry import register_router
register_router(router)

__all__ = ["SysPosition", "PositionVO", "PositionPageParam", "PositionDao", "PositionService", "router"]
