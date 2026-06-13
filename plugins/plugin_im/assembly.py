from __future__ import annotations

from sdk.kernel.plugin.loader import register_plugin_class
from sdk.kernel.registry import register_router

from .broadcast import client_router as broadcast_client_router, router as broadcast_router
from .friend import client_router as friend_client_router, router as friend_router
from .group import client_router as group_client_router, router as group_router
from .message import client_router as message_client_router, router as message_router
from .plugin import IMPlugin, ws_router


_registered = False


def register() -> None:
    global _registered
    if _registered:
        return

    register_plugin_class(IMPlugin)
    for router in (
        ws_router,
        broadcast_router,
        broadcast_client_router,
        friend_router,
        friend_client_router,
        group_router,
        group_client_router,
        message_router,
        message_client_router,
    ):
        register_router(router)

    _registered = True
