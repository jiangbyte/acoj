from __future__ import annotations

from plugins.assembly import register_plugins
from sdk.kernel.plugin.core_plugins import CORE_PLUGIN_CLASSES
from sdk.kernel.plugin.loader import register_plugin_class


_registered = False


def register_application() -> None:
    global _registered
    if _registered:
        return

    for plugin_class in CORE_PLUGIN_CLASSES:
        register_plugin_class(plugin_class)
    register_plugins()
    _registered = True
