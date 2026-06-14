from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any


@dataclass(slots=True)
class ExtensionSnapshot:
    points: dict[str, list[str]] = field(default_factory=dict)


class ExtensionRegistry:
    def __init__(self) -> None:
        self._entries: dict[str, dict[str, Any]] = {}

    def register(self, point: str, plugin_name: str, service: Any) -> None:
        key = self._key(point, plugin_name)
        self._entries[key] = {
            "point": point,
            "plugin_name": plugin_name,
            "service": service,
        }

    def resolve(self, point: str) -> list[Any]:
        return [
            entry["service"]
            for entry in self._entries.values()
            if entry["point"] == point
        ]

    def resolve_one(self, point: str) -> Any | None:
        services = self.resolve(point)
        return services[0] if services else None

    def snapshot(self) -> ExtensionSnapshot:
        grouped: dict[str, list[str]] = {}
        for entry in self._entries.values():
            grouped.setdefault(entry["point"], []).append(entry["plugin_name"])
        return ExtensionSnapshot(
            points={key: sorted(values) for key, values in grouped.items()},
        )

    def reset(self) -> None:
        self._entries.clear()

    @staticmethod
    def _key(point: str, plugin_name: str) -> str:
        return f"{point}:{plugin_name}"


_registry = ExtensionRegistry()


def register_extension(point: str, plugin_name: str, service: Any) -> None:
    _registry.register(point, plugin_name, service)


def resolve_extensions(point: str) -> list[Any]:
    return _registry.resolve(point)


def resolve_extension(point: str) -> Any | None:
    return _registry.resolve_one(point)


def extension_snapshot() -> ExtensionSnapshot:
    return _registry.snapshot()


def reset_extensions() -> None:
    _registry.reset()
