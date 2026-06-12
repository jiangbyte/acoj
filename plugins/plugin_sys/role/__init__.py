from .models import SysRole
from .params import RoleVO, RolePageParam, GrantPermissionParam, GrantResourceParam
from .repository import RoleRepository
from .service import RoleService, get_role_service
from .api import v1_router as router

from sdk.kernel.registry import register_router
register_router(router)

__all__ = ["SysRole", "RoleVO", "RolePageParam", "GrantPermissionParam", "GrantResourceParam", "RoleRepository", "RoleService", "get_role_service", "router"]
