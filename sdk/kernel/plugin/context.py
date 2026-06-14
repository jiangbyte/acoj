from __future__ import annotations

from dataclasses import dataclass
from typing import Any

from fastapi import APIRouter

from sdk.kernel.registry import register_router

from .extensions import register_extension


@dataclass(slots=True)
class PluginContext:
    name: str
    settings: dict[str, Any]

    def register_router(self, router: APIRouter) -> None:
        register_router(router)

    def register_extension(self, point: str, service: Any) -> None:
        register_extension(point, self.name, service)
