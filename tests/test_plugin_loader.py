from __future__ import annotations

import pytest

from sdk.kernel.plugin.interface import HeiPlugin, PluginInfo
from sdk.kernel.plugin.loader import PluginRegistry


class _Plugin(HeiPlugin):
    plugin_name = "base"
    events: list[str] = []
    fail_start = False
    fail_stop = False

    @classmethod
    def info(cls) -> PluginInfo:
        return PluginInfo(name=cls.plugin_name)

    async def on_start(self) -> None:
        type(self).events.append(f"start:{self.name()}")
        if type(self).fail_start:
            raise RuntimeError(f"{self.name()} start failed")

    async def on_stop(self) -> None:
        type(self).events.append(f"stop:{self.name()}")
        if type(self).fail_stop:
            raise RuntimeError(f"{self.name()} stop failed")


class _PluginA(_Plugin):
    plugin_name = "plugin-a"


class _PluginB(_Plugin):
    plugin_name = "plugin-b"


@pytest.mark.asyncio
async def test_start_all_continues_after_failure() -> None:
    registry = PluginRegistry()
    _Plugin.events = []
    _PluginA.fail_start = True
    _PluginB.fail_start = False

    registry.register_class(_PluginA)
    registry.register_class(_PluginB)
    registry.init_all()

    with pytest.raises(RuntimeError):
        await registry.start_all()

    assert _Plugin.events == ["start:plugin-a", "start:plugin-b"]
    snapshot = registry.snapshot()
    assert snapshot[0]["start_ok"] is False
    assert snapshot[1]["start_ok"] is True


@pytest.mark.asyncio
async def test_stop_all_continues_after_failure() -> None:
    registry = PluginRegistry()
    _Plugin.events = []
    _PluginA.fail_start = False
    _PluginB.fail_start = False
    _PluginA.fail_stop = True
    _PluginB.fail_stop = False

    registry.register_class(_PluginA)
    registry.register_class(_PluginB)
    registry.init_all()
    await registry.start_all()

    with pytest.raises(RuntimeError):
        await registry.stop_all()

    assert _Plugin.events[-2:] == ["stop:plugin-b", "stop:plugin-a"]
