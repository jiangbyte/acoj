from .models import SysBanner
from .params import BannerVO, BannerPageParam
from .dao import BannerDao
from .service import BannerService
from . import migrate
from .api import v1_router as router

from core.plugin.registry import register_router
register_router(router)

__all__ = ["SysBanner", "BannerVO", "BannerPageParam", "BannerDao", "BannerService", "router"]
