from .models import SysDict
from .params import DictVO, DictTreeVO, DictPageParam, DictListParam, DictTreeParam
from .repository import DictRepository
from .service import DictService, get_dict_service
from .api import v1_router as router

from sdk.kernel.registry import register_router
register_router(router)

__all__ = ["SysDict", "DictVO", "DictTreeVO", "DictPageParam", "DictListParam", "DictTreeParam", "DictRepository", "DictService", "get_dict_service", "router"]
