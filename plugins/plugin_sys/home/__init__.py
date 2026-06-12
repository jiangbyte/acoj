from .models import SysQuickAction
from .params import (
    QuickActionVO,
    AddQuickActionParam,
    RemoveQuickActionParam,
    SortQuickActionParam,
    HomeNotice,
    HomeStats,
    HomeVO,
)
from .repository import QuickActionRepository
from .service import HomeService, get_home_service
from .api import v1_router as router

from sdk.kernel.registry import register_router
register_router(router)

__all__ = [
    "SysQuickAction",
    "QuickActionVO",
    "AddQuickActionParam",
    "RemoveQuickActionParam",
    "SortQuickActionParam",
    "HomeNotice",
    "HomeStats",
    "HomeVO",
    "QuickActionRepository",
    "HomeService",
    "get_home_service",
    "router",
]
