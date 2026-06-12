from .models import SysUser
from .params import (
    UserVO, UserPageParam, GrantRoleParam,
    GrantUserPermissionParam, UpdateStatusParam,
    BatchImportParam, BatchImportUser,
    UpdateProfileParam, UpdateAvatarParam, UpdatePasswordParam,
)
from .dao import UserDao
from .service import (
    UserService, LoginUserApiProvider,
    user_find_by_id, user_find_by_username, user_find_by_email,
    user_to_login_info, user_record_login,
    user_page, user_create, user_modify, user_remove, user_detail,
    user_grant_roles, user_grant_permissions,
    user_get_permission_details, user_get_role_ids,
    user_get_current, user_get_menus, user_get_permissions,
    user_update_profile, user_update_avatar, user_update_password,
    user_reset_password, user_batch_import, user_update_status,
    user_export,
)
from . import migrate
from .api import v1_router as router

from core.plugin.registry import register_router
register_router(router)

__all__ = [
    "SysUser", "UserVO", "UserPageParam", "GrantRoleParam",
    "GrantUserPermissionParam", "UpdateStatusParam",
    "BatchImportParam", "BatchImportUser",
    "UpdateProfileParam", "UpdateAvatarParam", "UpdatePasswordParam",
    "UserDao", "UserService", "LoginUserApiProvider",
    "user_find_by_id", "user_find_by_username", "user_find_by_email",
    "user_to_login_info", "user_record_login",
    "user_page", "user_create", "user_modify", "user_remove", "user_detail",
    "user_grant_roles", "user_grant_permissions",
    "user_get_permission_details", "user_get_role_ids",
    "user_get_current", "user_get_menus", "user_get_permissions",
    "user_update_profile", "user_update_avatar", "user_update_password",
    "user_reset_password", "user_batch_import", "user_update_status",
    "user_export",
    "router",
]
