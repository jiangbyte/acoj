from __future__ import annotations

from abc import ABC
from dataclasses import dataclass, field


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
    initialized: bool = False
    started: bool = False
    init_ok: bool = False
    start_ok: bool = False
    last_error: str = ""


class HeiPlugin(ABC):
    @classmethod
    def info(cls) -> PluginInfo:
        return PluginInfo(name=cls.__name__)

    def name(self) -> str:
        return self.info().name

    def on_init(self) -> None:
        return None

    async def on_start(self) -> None:
        return None

    async def on_stop(self) -> None:
        return None


class NoopPlugin(HeiPlugin):
    pass
