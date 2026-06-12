from .models import SysNotice
from .params import NoticeVO, NoticePageParam
from .repository import NoticeRepository
from .service import NoticeService
from .api import v1_router as router

from sdk.kernel.registry import register_router
register_router(router)

__all__ = ["SysNotice", "NoticeVO", "NoticePageParam", "NoticeRepository", "NoticeService", "router"]
