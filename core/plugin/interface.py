"""
HeiPlugin base class — defines the plugin lifecycle interface.

Comprehensive alignment with hei-gin's ``sdk/module/module.go``:

| hei-gin                 | Python                     |
|--------------------------|----------------------------|
| ``Module`` interface     | ``HeiPlugin`` ABC          |
| ``Name() string``        | ``info() -> PluginInfo``   |
| ``Init() error``         | ``on_init()``              |
| ``Start() error``        | ``on_start()`` (async)     |
| ``Stop() error``         | ``on_stop()`` (async)      |
| ``NoopModule``           | ``NoopPlugin``             |
| ``Register(m)``          | ``__init_subclass__``      |
"""

from __future__ import annotations

from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from typing import Optional


@dataclass
class PluginInfo:
    """Plugin metadata. Mirrors hei-gin's ``api.PluginInfo``."""
    name: str
    version: str = "1.0.0"
    description: str = ""
    settings_prefix: Optional[str] = None
    dependencies: list[str] = field(default_factory=list)


# Module-level registration — equivalent to hei-gin's ``var modules []Module``
_plugin_registry: list[type[HeiPlugin]] = []
_plugin_instances: list[HeiPlugin] = []


class HeiPlugin(ABC):
    """Abstract base plugin.

    Every concrete subclass is auto-registered via ``__init_subclass__``,
    mirroring hei-gin's ``module.Register(m)`` called from ``init()``.
    """

    def __init_subclass__(cls, **kwargs):
        super().__init_subclass__(**kwargs)
        abstract_methods = getattr(cls, "__abstractmethods__", frozenset())
        if not abstract_methods and cls is not HeiPlugin:
            _plugin_registry.append(cls)

    # ── Lifecycle hooks ─────────────────────────────────────────────

    @classmethod
    @abstractmethod
    def info(cls) -> PluginInfo:
        """Return plugin metadata.  Implement as @classmethod."""
        ...

    def on_init(self) -> None:
        """Synchronous initialisation hook.

        Called once per plugin instance, in registration order,
        after infra (DB, Redis, config) is ready but before the
        HTTP server starts.  Mirrors hei-gin's ``Init() error``.
        """

    async def on_start(self) -> None:
        """Async startup hook.

        Called after the HTTP server has started.
        Suitable for background tasks, cron, etc.
        Mirrors hei-gin's ``Start() error``.
        """

    async def on_stop(self) -> None:
        """Async shutdown hook.

        Called in reverse registration order during graceful shutdown.
        Mirrors hei-gin's ``Stop() error``.
        """


class NoopPlugin(HeiPlugin):
    """Plugin with no-op lifecycle hooks.

    Subclass this instead of ``HeiPlugin`` when you only need ``info()``.
    Mirrors hei-gin's ``module.NoopModule``.
    """

    @classmethod
    def info(cls) -> PluginInfo:
        return PluginInfo(name=cls.__name__, version="1.0.0")


# ── Accessors (used by the loader) ───────────────────────────────────

def get_registered_plugins() -> list[type[HeiPlugin]]:
    """Return registered plugin classes in registration order."""
    return list(_plugin_registry)


def get_plugin_instances() -> list[HeiPlugin]:
    """Return instantiated plugin objects in registration order."""
    return list(_plugin_instances)


def instantiate_plugins() -> None:
    """Create one instance per registered plugin class.

    Called once by the loader during ``discover_and_load()``.
    """
    if _plugin_instances:
        return  # already instantiated
    for cls in _plugin_registry:
        _plugin_instances.append(cls())
