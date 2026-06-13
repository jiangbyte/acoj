from __future__ import annotations

from sdk.kernel.app.health import router as health_router
from sdk.kernel.plugin.loader import register_plugin_class
from sdk.kernel.registry import register_router

from .analyze import router as analyze_router
from .auth.captcha import router as auth_captcha_router
from .auth.sm2 import router as auth_sm2_router
from .auth.username import router as auth_username_router
from .banner import router as banner_router
from .config import router as config_router
from .dict import router as dict_router
from .file import client_router as file_client_router, router as file_router
from .group import router as group_router
from .home import router as home_router
from .log import router as log_router
from .notice import router as notice_router
from .org import router as org_router
from .permission import router as permission_router
from .plugin import SysPlugin
from .position import router as position_router
from .resource import router as resource_router
from .role import router as role_router
from .session import router as session_router
from .user import router as user_router


_registered = False


def register() -> None:
    global _registered
    if _registered:
        return

    register_plugin_class(SysPlugin)
    for router in (
        health_router,
        user_router,
        role_router,
        org_router,
        group_router,
        position_router,
        dict_router,
        config_router,
        banner_router,
        home_router,
        log_router,
        notice_router,
        file_router,
        file_client_router,
        resource_router,
        session_router,
        permission_router,
        analyze_router,
        auth_username_router,
        auth_captcha_router,
        auth_sm2_router,
    ):
        register_router(router)

    _registered = True
