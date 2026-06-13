from __future__ import annotations

import logging
from importlib import import_module
from pathlib import Path

logger = logging.getLogger(__name__)

PLUGIN_PREFIX = "plugin_"
BUILTIN_PLUGIN_ORDER = ("plugin_sys", "plugin_client", "plugin_im")


def _discover_plugin_names() -> list[str]:
    base_dir = Path(__file__).resolve().parent
    discovered = {
        item.name
        for item in base_dir.iterdir()
        if item.is_dir()
        and item.name.startswith(PLUGIN_PREFIX)
        and not item.name.startswith("_")
        and (item / "assembly.py").is_file()
    }
    ordered = [name for name in BUILTIN_PLUGIN_ORDER if name in discovered]
    ordered.extend(sorted(discovered.difference(BUILTIN_PLUGIN_ORDER)))
    return ordered


def register_plugins() -> None:
    for plugin_name in _discover_plugin_names():
        module = import_module(f"plugins.{plugin_name}.assembly")
        register = getattr(module, "register", None)
        if not callable(register):
            raise RuntimeError(f"plugin assembly has no register() function: {plugin_name}")
        register()
        logger.info("[PluginAssembly] registered %s", plugin_name)
