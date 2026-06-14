from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any, Callable

from fastapi import APIRouter, FastAPI
from sqlalchemy.orm import DeclarativeBase

from sdk.infra.db.migrate import register_model as register_migrate_model


def _fn_name(fn: Callable[..., Any]) -> str:
    return getattr(fn, "__module__", "") + "." + getattr(fn, "__qualname__", repr(fn))


@dataclass
class RegistrySnapshot:
    routes: list[str] = field(default_factory=list)
    middlewares: list[str] = field(default_factory=list)
    permissions: list[str] = field(default_factory=list)
    models: list[str] = field(default_factory=list)
    frozen: bool = False


class AssemblyRegistry:
    def __init__(self) -> None:
        self._route_items: list[tuple[str, Callable[[FastAPI], None]]] = []
        self._middleware_items: list[tuple[str, Callable[[FastAPI], None]]] = []
        self._permission_entries: dict[str, dict[str, str]] = {}
        self._models: dict[str, type] = {}
        self._route_index: set[str] = set()
        self._middleware_index: set[str] = set()
        self._frozen = False

    @property
    def frozen(self) -> bool:
        return self._frozen

    def freeze(self) -> None:
        self._frozen = True

    def ensure_mutable(self) -> None:
        if self._frozen:
            raise RuntimeError("assembly registry is frozen")

    def register_route(self, fn: Callable[[FastAPI], None]) -> None:
        self.ensure_mutable()
        name = _fn_name(fn)
        if name in self._route_index:
            raise RuntimeError(f"duplicate route registrar: {name}")
        self._route_index.add(name)
        self._route_items.append((name, fn))

    def register_router(self, router: APIRouter) -> None:
        self.ensure_mutable()
        name = f"router:{id(router)}:{getattr(router, 'prefix', '')}"
        if name in self._route_index:
            return

        def registrar(app: FastAPI) -> None:
            app.include_router(router)

        self._route_index.add(name)
        self._route_items.append((name, registrar))

    def execute_routes(self, app: FastAPI) -> None:
        self.freeze()
        for _, fn in self._route_items:
            fn(app)

    def register_middleware(self, fn: Callable[[FastAPI], None]) -> None:
        self.ensure_mutable()
        name = _fn_name(fn)
        if name in self._middleware_index:
            raise RuntimeError(f"duplicate middleware registrar: {name}")
        self._middleware_index.add(name)
        self._middleware_items.append((name, fn))

    def execute_middlewares(self, app: FastAPI) -> None:
        self.freeze()
        for _, fn in self._middleware_items:
            fn(app)

    def register_permission(self, code: str, name: str = "", module: str = "") -> None:
        self.ensure_mutable()
        if code not in self._permission_entries:
            self._permission_entries[code] = {
                "code": code,
                "name": name,
                "module": module or _module_from_code(code),
            }

    def register_model(self, model_class: type) -> None:
        table_name = getattr(model_class, "__tablename__", "")
        if not table_name:
            return
        self._models[table_name] = model_class
        register_migrate_model(model_class)

    def snapshot(self) -> RegistrySnapshot:
        return RegistrySnapshot(
            routes=[name for name, _ in self._route_items],
            middlewares=[name for name, _ in self._middleware_items],
            permissions=sorted(self._permission_entries),
            models=sorted(self._models),
            frozen=self._frozen,
        )

    def get_permission_entries(self) -> dict[str, dict[str, str]]:
        return dict(self._permission_entries)

    def get_models(self) -> dict[str, type]:
        return dict(self._models)

    def reset(self) -> None:
        self.__init__()


def _module_from_code(code: str) -> str:
    parts = code.split(":")
    return ":".join(parts[:-1]) if len(parts) > 1 else code


_registry = AssemblyRegistry()


def register_route(fn: Callable[[FastAPI], None]) -> None:
    _registry.register_route(fn)


def register_router(router: APIRouter) -> None:
    _registry.register_router(router)


def execute_routes(app: FastAPI) -> None:
    _registry.execute_routes(app)


def register_middleware(fn: Callable[[FastAPI], None]) -> None:
    _registry.register_middleware(fn)


def execute_middlewares(app: FastAPI) -> None:
    _registry.execute_middlewares(app)


def freeze() -> None:
    _registry.freeze()


def snapshot_state() -> RegistrySnapshot:
    return _registry.snapshot()


def register_model(model_class: type) -> None:
    _registry.register_model(model_class)


def get_registered_models() -> dict[str, type]:
    return _registry.get_models()


def get_registered_perm_entries() -> dict[str, dict[str, str]]:
    return _registry.get_permission_entries()


def reset_for_test() -> None:
    _registry.reset()


def Perm(code: str, name: str = "", realm_id: str = "BUSINESS") -> Callable[..., Any]:
    _registry.register_permission(code, name=name, module=_module_from_code(code))
    from sdk.auth.decorator import CheckPermission

    return CheckPermission(code, realm_id=realm_id)


def ClientPerm(code: str, name: str = "") -> Callable[..., Any]:
    return Perm(code, name, realm_id="CONSUMER")


class HeiBase(DeclarativeBase):
    pass
