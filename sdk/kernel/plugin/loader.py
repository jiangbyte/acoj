from __future__ import annotations

import importlib
from dataclasses import asdict
from pathlib import Path
from typing import Any

from sdk.kernel.registry import register_router

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
        info = plugin_class.info()
        name = info.name
        if self._frozen:
            raise RuntimeError(f"plugin registry is frozen: {name}")
        if name in self._plugin_class_index:
            raise RuntimeError(f"duplicate plugin: {name}")
        self._plugin_class_index.add(name)
        self._plugin_classes.append(plugin_class)
        self._status[name] = PluginStatus(
            name=name,
            version=info.version,
            dependencies=list(info.dependencies),
            settings_prefix=info.settings_prefix,
            enabled=True,
        )

    def discover(self) -> None:
        if self._discovered:
            return
        assembly = importlib.import_module("sdk.kernel.assembly")
        assembly.register_application()
        self._discover_project_plugins()
        self._discovered = True

    def instantiate(self) -> None:
        if self._instances:
            return
        self._plugin_classes = self._resolve_plugin_order()
        for plugin_class in self._plugin_classes:
            self._instances.append(plugin_class())

    def freeze(self) -> None:
        self._frozen = True

    def snapshot(self) -> list[dict[str, Any]]:
        return [asdict(self._status[plugin_class.info().name]) for plugin_class in self._plugin_classes]

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
            status.phase = "initializing"
            try:
                plugin.on_init()
                status.initialized = True
                status.init_ok = True
                status.phase = "initialized"
            except Exception as exc:
                status.initialized = True
                status.init_ok = False
                status.phase = "init_failed"
                status.last_error = str(exc)
                raise
        self._initialized = True

    async def start_all(self) -> None:
        if not self._initialized:
            raise RuntimeError("plugins must be initialized before start")
        started_plugins: list[HeiPlugin] = []
        first_error: Exception | None = None
        for plugin in self._instances:
            status = self._status[plugin.name()]
            status.phase = "starting"
            try:
                await plugin.on_start()
                status.started = True
                status.start_ok = True
                status.phase = "started"
                started_plugins.append(plugin)
            except Exception as exc:
                status.started = True
                status.start_ok = False
                status.phase = "start_failed"
                status.last_error = str(exc)
                if first_error is None:
                    first_error = RuntimeError(f"plugin {plugin.name()} start failed: {exc}")
                break
        if first_error is not None:
            await self._rollback_started(started_plugins)
            raise first_error

    async def stop_all(self) -> None:
        first_error: Exception | None = None
        for plugin in reversed(self._instances):
            status = self._status[plugin.name()]
            if not status.start_ok:
                continue
            status.phase = "stopping"
            try:
                await plugin.on_stop()
                status.phase = "stopped"
            except Exception as exc:
                status.last_error = str(exc)
                status.phase = "stop_failed"
                if first_error is None:
                    first_error = RuntimeError(f"plugin {plugin.name()} stop failed: {exc}")
        if first_error is not None:
            raise first_error

    def reset(self) -> None:
        self.__init__()

    def _resolve_plugin_order(self) -> list[type[HeiPlugin]]:
        plugin_map = {plugin_class.info().name: plugin_class for plugin_class in self._plugin_classes}
        dependency_map = {
            name: list(plugin_class.info().dependencies)
            for name, plugin_class in plugin_map.items()
        }

        for name, dependencies in dependency_map.items():
            missing = [dependency for dependency in dependencies if dependency not in plugin_map]
            if missing:
                raise RuntimeError(f"plugin {name} missing dependencies: {', '.join(missing)}")

        resolved: list[type[HeiPlugin]] = []
        visiting: set[str] = set()
        visited: set[str] = set()

        def visit(name: str) -> None:
            if name in visited:
                return
            if name in visiting:
                raise RuntimeError(f"plugin dependency cycle detected: {name}")
            visiting.add(name)
            for dependency in dependency_map.get(name, []):
                visit(dependency)
            visiting.remove(name)
            visited.add(name)
            resolved.append(plugin_map[name])

        for plugin_class in self._plugin_classes:
            visit(plugin_class.info().name)
        return resolved

    async def _rollback_started(self, started_plugins: list[HeiPlugin]) -> None:
        for plugin in reversed(started_plugins):
            status = self._status[plugin.name()]
            try:
                await plugin.on_stop()
                status.phase = "rolled_back"
            except Exception as exc:
                status.last_error = str(exc)
                status.phase = "rollback_failed"

    def _discover_project_plugins(self) -> None:
        plugins_root = Path.cwd() / "plugins"
        if not plugins_root.is_dir():
            return

        for plugin_path in sorted(plugins_root.glob("plugin_*/plugin.py")):
            module_name = ".".join(plugin_path.relative_to(Path.cwd()).with_suffix("").parts)
            module = importlib.import_module(module_name)
            plugin_class = self._resolve_module_plugin_class(module_name, module)
            self.register_class(plugin_class)
            for router in plugin_class.routers():
                register_router(router)

    def _resolve_module_plugin_class(self, module_name: str, module: Any) -> type[HeiPlugin]:
        candidates = [
            value for value in vars(module).values()
            if isinstance(value, type)
            and issubclass(value, HeiPlugin)
            and value is not HeiPlugin
            and value.__module__ == module.__name__
        ]
        if not candidates:
            raise RuntimeError(f"plugin class not found in module: {module_name}")
        if len(candidates) > 1:
            names = ", ".join(sorted(candidate.__name__ for candidate in candidates))
            raise RuntimeError(f"multiple plugin classes found in module {module_name}: {names}")
        return candidates[0]


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


def plugin_status(name: str) -> dict[str, Any] | None:
    snapshot = {item["name"]: item for item in _plugins.snapshot()}
    return snapshot.get(name)


def reset_for_test() -> None:
    _plugins.reset()
