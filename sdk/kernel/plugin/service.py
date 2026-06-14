from __future__ import annotations

from dataclasses import asdict

from .extensions import extension_snapshot
from .loader import plugin_snapshot


class PluginService:
    def list_plugins(self) -> list[dict]:
        return plugin_snapshot()

    def list_extensions(self) -> dict[str, list[str]]:
        return asdict(extension_snapshot())["points"]


plugin_service = PluginService()
