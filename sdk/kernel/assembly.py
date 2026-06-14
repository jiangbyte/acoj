from __future__ import annotations

from sdk.kernel.plugin.core_plugins import CORE_PLUGIN_CLASSES
from sdk.kernel.plugin.loader import register_plugin_class
from sdk.kernel.plugin.api import router as plugin_debug_router
from sdk.kernel.registry import register_router


_registered = False


def register_application() -> None:
    global _registered
    if _registered:
        return

    for plugin_class in CORE_PLUGIN_CLASSES:
        register_plugin_class(plugin_class)
    register_router(plugin_debug_router)
    _registered = True
