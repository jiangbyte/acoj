from __future__ import annotations

from abc import ABC
from dataclasses import dataclass, field
from typing import Any

from sdk.config.settings import settings
from .context import PluginContext


@dataclass
class PluginInfo:
    name: str
    version: str = "1.0.0"
    description: str = ""
    settings_prefix: str | None = None
    dependencies: list[str] = field(default_factory=list)


@dataclass
class PluginStatus:
    name: str
    version: str = ""
    dependencies: list[str] = field(default_factory=list)
    settings_prefix: str | None = None
    enabled: bool = True
    initialized: bool = False
    started: bool = False
    init_ok: bool = False
    start_ok: bool = False
    phase: str = "registered"
    last_error: str = ""


class HeiPlugin(ABC):
    def __init__(self) -> None:
        self._context = PluginContext(
            name=self.info().name,
            settings=self.settings(),
        )

    @classmethod
    def info(cls) -> PluginInfo:
        return PluginInfo(name=cls.__name__)

    @classmethod
    def routers(cls) -> tuple[Any, ...]:
        return ()

    def name(self) -> str:
        return self.info().name

    def settings(self) -> dict[str, Any]:
        info = self.info()
        return settings.get_plugin_settings(info.name, info.settings_prefix)

    @property
    def context(self) -> PluginContext:
        return self._context

    def on_init(self) -> None:
        return None

    async def on_start(self) -> None:
        return None

    async def on_stop(self) -> None:
        return None


class NoopPlugin(HeiPlugin):
    pass
