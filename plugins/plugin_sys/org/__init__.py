from .models import SysOrg
from .params import OrgVO, OrgPageParam
from .repository import OrgRepository
from .service import OrgService
from .api import v1_router as router

from core.plugin.registry import register_router
register_router(router)

__all__ = ["SysOrg", "OrgVO", "OrgPageParam", "OrgRepository", "OrgService", "router"]
