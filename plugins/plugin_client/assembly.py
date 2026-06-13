from __future__ import annotations

from sdk.kernel.plugin.loader import register_plugin_class
from sdk.kernel.registry import register_router

from .auth.captcha import router as auth_captcha_router
from .auth.sm2 import router as auth_sm2_router
from .auth.username import router as auth_username_router
from .plugin import ClientPlugin
from .session import router as session_router
from .user import router as user_router


_registered = False


def register() -> None:
    global _registered
    if _registered:
        return

    register_plugin_class(ClientPlugin)
    for router in (
        session_router,
        user_router,
        auth_captcha_router,
        auth_sm2_router,
        auth_username_router,
    ):
        register_router(router)

    _registered = True
