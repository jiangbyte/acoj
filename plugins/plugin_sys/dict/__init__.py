from .models import SysDict
from .params import DictVO, DictTreeVO, DictPageParam, DictListParam, DictTreeParam
from .dao import DictDao
from .service import DictService
from .api import v1_router as router

from core.plugin.registry import register_router
register_router(router)

__all__ = ["SysDict", "DictVO", "DictTreeVO", "DictPageParam", "DictListParam", "DictTreeParam", "DictDao", "DictService", "router"]
