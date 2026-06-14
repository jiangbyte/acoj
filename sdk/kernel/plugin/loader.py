from __future__ import annotations

import importlib
from dataclasses import asdict
from typing import Any

from .interface import HeiPlugin, PluginStatus


class PluginRegistry:
    def __init__(self) -> None:
        self._plugin_classes: list[type[HeiPlugin]] = []
        self._plugin_class_index: set[str] = set()
        self._instances: list[HeiPlugin] = []
        self._status: dict[str, PluginStatus] = {}
        self._frozen = False
        self._initialized = False
        self._discovered = False

    def register_class(self, plugin_class: type[HeiPlugin]) -> None:
        name = plugin_class.info().name
        if self._frozen:
            raise RuntimeError(f"plugin registry is frozen: {name}")
        if name in self._plugin_class_index:
            raise RuntimeError(f"duplicate plugin: {name}")
        self._plugin_class_index.add(name)
        self._plugin_classes.append(plugin_class)
        self._status[name] = PluginStatus(name=name)

    def discover(self) -> None:
        if self._discovered:
            return
        assembly = importlib.import_module("sdk.kernel.assembly")
        assembly.register_application()
        self._discovered = True

    def instantiate(self) -> None:
        if self._instances:
            return
        for plugin_class in self._plugin_classes:
            self._instances.append(plugin_class())

    def freeze(self) -> None:
        self._frozen = True

    def snapshot(self) -> list[dict[str, Any]]:
        return [asdict(self._status[plugin.name()]) for plugin in self._instances]

    def ready(self) -> tuple[bool, list[dict[str, Any]]]:
        snapshot = self.snapshot()
        ok = all(item["init_ok"] and item["start_ok"] for item in snapshot)
        return ok, snapshot

    def init_all(self) -> None:
        if not self._plugin_classes:
            self.discover()
        self.instantiate()
        self.freeze()
        for plugin in self._instances:
            status = self._status[plugin.name()]
            try:
                plugin.on_init()
                status.initialized = True
                status.init_ok = True
            except Exception as exc:
                status.initialized = True
                status.init_ok = False
                status.last_error = str(exc)
                raise
        self._initialized = True

    async def start_all(self) -> None:
        if not self._initialized:
            raise RuntimeError("plugins must be initialized before start")
        first_error: Exception | None = None
        for plugin in self._instances:
            status = self._status[plugin.name()]
            try:
                await plugin.on_start()
                status.started = True
                status.start_ok = True
            except Exception as exc:
                status.started = True
                status.start_ok = False
                status.last_error = str(exc)
                if first_error is None:
                    first_error = RuntimeError(f"plugin {plugin.name()} start failed: {exc}")
        if first_error is not None:
            raise first_error

    async def stop_all(self) -> None:
        first_error: Exception | None = None
        for plugin in reversed(self._instances):
            status = self._status[plugin.name()]
            try:
                await plugin.on_stop()
            except Exception as exc:
                status.last_error = str(exc)
                if first_error is None:
                    first_error = RuntimeError(f"plugin {plugin.name()} stop failed: {exc}")
        if first_error is not None:
            raise first_error

    def reset(self) -> None:
        self.__init__()


_plugins = PluginRegistry()


def register_plugin_class(plugin_class: type[HeiPlugin]) -> None:
    _plugins.register_class(plugin_class)


def discover_and_load() -> None:
    _plugins.discover()


def init_plugins() -> None:
    _plugins.init_all()


async def start_plugins(app: Any | None = None) -> None:
    await _plugins.start_all()


async def stop_plugins(app: Any | None = None) -> None:
    await _plugins.stop_all()


def freeze_plugins() -> None:
    _plugins.freeze()


def plugin_snapshot() -> list[dict[str, Any]]:
    return _plugins.snapshot()


def plugins_ready() -> tuple[bool, list[dict[str, Any]]]:
    return _plugins.ready()


def reset_for_test() -> None:
    _plugins.reset()
