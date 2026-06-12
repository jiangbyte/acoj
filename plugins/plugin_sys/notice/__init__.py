from .models import SysNotice
from .params import NoticeVO, NoticePageParam
from .dao import NoticeDao
from .service import NoticeService
from . import migrate
from .api import v1_router as router

from core.plugin.registry import register_router
register_router(router)

__all__ = ["SysNotice", "NoticeVO", "NoticePageParam", "NoticeDao", "NoticeService", "router"]
