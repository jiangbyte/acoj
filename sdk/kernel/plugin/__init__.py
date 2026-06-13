# ── Plugin framework ────────────────────────────────────────────────
from .interface import HeiPlugin, NoopPlugin, PluginInfo
from sdk.kernel.registry import (
    HeiBase, register_model, get_registered_models,
    register_route, register_router, execute_routes,
    register_middleware, execute_middlewares,
    Perm, ClientPerm, get_registered_perm_entries,
)
from .loader import (
    register_plugin_class,
    discover_and_load,
    init_plugins,
    start_plugins,
    stop_plugins,
    plugin_snapshot,
    plugins_ready,
    freeze_plugins,
)
from sdk.infra.eventbus import EventBus, subscribe, publish

__all__ = [
    "HeiPlugin", "NoopPlugin", "PluginInfo",
    "HeiBase", "register_model", "get_registered_models",
    "register_route", "register_router", "execute_routes",
    "register_middleware", "execute_middlewares",
    "Perm", "ClientPerm", "get_registered_perm_entries",
    "register_plugin_class",
    "discover_and_load", "init_plugins", "start_plugins", "stop_plugins",
    "plugin_snapshot", "plugins_ready", "freeze_plugins",
    "EventBus", "subscribe", "publish",
]
