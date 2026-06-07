from .models import SysOrg
from .params import OrgVO, OrgPageParam
from .dao import OrgDao
from .service import OrgService
from .api import v1_router as router

from core.plugin.registry import register_router
register_router(router)

__all__ = ["SysOrg", "OrgVO", "OrgPageParam", "OrgDao", "OrgService", "router"]
