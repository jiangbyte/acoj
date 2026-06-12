from .models import SysConfig
from .params import (
    ConfigVO,
    ConfigPageParam,
    ConfigListParam,
    ConfigBatchEditItem,
    ConfigBatchEditParam,
    ConfigCategoryEditParam,
    SysConfigToConfigVO,
)
from .repository import ConfigRepository
from .service import ConfigService, get_config_service, get_value_by_key
from .api.v1 import router

from sdk.kernel.registry import register_router
register_router(router)

__all__ = [
    "SysConfig",
    "ConfigVO",
    "ConfigPageParam",
    "ConfigListParam",
    "ConfigBatchEditItem",
    "ConfigBatchEditParam",
    "ConfigCategoryEditParam",
    "SysConfigToConfigVO",
    "ConfigRepository",
    "ConfigService",
    "get_config_service",
    "get_value_by_key",
    "router",
]
