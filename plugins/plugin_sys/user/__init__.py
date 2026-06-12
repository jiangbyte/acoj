from .models import SysUser
from .params import (
    UserVO, UserPageParam, GrantRoleParam,
    GrantUserPermissionParam, UpdateStatusParam,
    BatchImportParam, BatchImportUser,
    UpdateProfileParam, UpdateAvatarParam, UpdatePasswordParam,
)
from .repository import UserRepository
from .service import (
    UserService,
    LoginUserApiProvider,
    get_user_service,
)
from .api import v1_router as router

from sdk.kernel.registry import register_router
register_router(router)

__all__ = [
    "SysUser", "UserVO", "UserPageParam", "GrantRoleParam",
    "GrantUserPermissionParam", "UpdateStatusParam",
    "BatchImportParam", "BatchImportUser",
    "UpdateProfileParam", "UpdateAvatarParam", "UpdatePasswordParam",
    "UserRepository", "UserService", "LoginUserApiProvider", "get_user_service",
    "router",
]
