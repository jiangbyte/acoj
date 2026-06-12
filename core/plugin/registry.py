"""
Global registries — mirrors hei-gin's ``sdk/registry/`` package.

Contains:

* ``register_route(fn)`` / ``execute_routes(app)``       —  hei-gin ``route.go``
* ``register_router(router)`` — convenience wrapper (APIRouter → register_route)
* ``register_middleware(fn)`` / ``execute_middlewares(app)``  —  ``middleware.go``
* ``Perm(code, name)`` / ``ClientPerm(code, name)``     —  ``perm.go`` (double-duty)
* ``HeiBase`` — model base with auto-registration
* ``register_model()``  —  explicit registration fallback
"""

from __future__ import annotations

import logging
from typing import Any, Callable

from fastapi import FastAPI, APIRouter
from sqlalchemy.orm import DeclarativeBase

from core.db.migrate import register_model as register_migrate_model

from core.enums import LoginTypeEnum

logger = logging.getLogger(__name__)


# ═════════════════════════════════════════════════════════════════════
# 1. Route registry  —  mirrors sdk/registry/route.go
# ═════════════════════════════════════════════════════════════════════

_route_registrars: list[Callable[[FastAPI], None]] = []


def register_route(fn: Callable[[FastAPI], None]) -> None:
    """Register a route registrar function.

    The function receives the ``FastAPI`` application instance and
    should call ``app.include_router(...)`` or add routes directly.

    Mirrors hei-gin's ``registry.RegisterRoute(reg RouteRegistrar)``.
    """
    _route_registrars.append(fn)


def register_router(router: APIRouter) -> None:
    """Convenience: wrap an ``APIRouter`` as a ``register_route()`` call.

    This is the idiomatic FastAPI equivalent of hei-gin's
    ``RegisterRoute()`` — you can pass a pre-built APIRouter
    and it will be mounted automatically.
    """
    def _registrar(app: FastAPI) -> None:
        app.include_router(router)

    _route_registrars.append(_registrar)


def execute_routes(app: FastAPI) -> None:
    """Execute all registered route registrars.

    Mirrors hei-gin's ``registry.ExecuteRoutes(r *gin.Engine)``.
    """
    for fn in _route_registrars:
        fn(app)


# ═════════════════════════════════════════════════════════════════════
# 2. Middleware registry  —  mirrors sdk/registry/middleware.go
# ═════════════════════════════════════════════════════════════════════

_middleware_registrars: list[Callable[[FastAPI], None]] = []


def register_middleware(fn: Callable[[FastAPI], None]) -> None:
    """Register a middleware factory.

    The function receives the ``FastAPI`` application instance and
    should call ``app.add_middleware(...)``.

    Mirrors hei-gin's ``registry.RegisterMiddleware(reg)``.
    """
    _middleware_registrars.append(fn)


def execute_middlewares(app: FastAPI) -> None:
    """Apply all registered middleware factories.

    Mirrors hei-gin's ``registry.ApplyMiddlewares(r)``.
    """
    for fn in _middleware_registrars:
        fn(app)


# ═════════════════════════════════════════════════════════════════════
# 3. Permission registry  —  mirrors sdk/registry/perm.go
#
#    ``Perm(code, name)`` does double-duty:
#      1. Registers the permission entry (for the permission scanner)
#      2. Returns a decorator that checks permission at runtime
# ═════════════════════════════════════════════════════════════════════

_perm_entries: dict[str, dict[str, str]] = {}


def _module_from_code(code: str) -> str:
    parts = code.split(":")
    return ":".join(parts[:-1]) if len(parts) > 1 else code


def Perm(code: str, name: str = "", login_type: str = LoginTypeEnum.BUSINESS) -> Callable:
    """Double-duty permission decorator.

    1. Registers ``(code, name, module)`` in the permission registry.
    2. Returns a decorator that checks the permission at runtime.

    Usage::

        @router.get("/api/v1/sys/user/page", summary="用户分页")
        @Perm("sys:user:page", "用户分页")
        async def page_handler(...): ...

    Mirrors hei-gin's ``registry.Perm(code, name)``.
    """
    if code not in _perm_entries:
        _perm_entries[code] = {
            "code": code,
            "name": name,
            "module": _module_from_code(code),
        }
        logger.debug("Perm registered: %s (%s)", code, name)

    from core.auth.decorator import HeiCheckPermission
    return HeiCheckPermission(code, login_type=login_type)


def ClientPerm(code: str, name: str = "") -> Callable:
    """Consumer-side double-duty permission decorator.

    Mirrors hei-gin's ``registry.ClientPerm(code, name)``.
    """
    return Perm(code, name, login_type=LoginTypeEnum.CONSUMER)


def get_registered_perm_entries() -> dict[str, dict[str, str]]:
    """Return all registered permission entries."""
    return dict(_perm_entries)


# ═════════════════════════════════════════════════════════════════════
# 4. Model registry  —  mirrors sdk/db/migrate.go RegisterModel()
# ═════════════════════════════════════════════════════════════════════

_models: dict[str, type] = {}


class HeiBase(DeclarativeBase):
    """Base class for ORM models.

    Subclasses are auto-registered, mirroring hei-gin's
    ``db.RegisterModel(&X{})`` pattern.

    Usage::

        from core.plugin.registry import HeiBase

        class SysUser(HeiBase):
            __tablename__ = "sys_user"
            ...
    """

    def __init_subclass__(cls, **kwargs):
        super().__init_subclass__(**kwargs)
        if hasattr(cls, "__tablename__") and cls.__tablename__:
            _models[cls.__tablename__] = cls
            register_migrate_model(cls)
            logger.debug("Model auto-registered: %s (%s)", cls.__name__, cls.__tablename__)


def register_model(model_class: type) -> None:
    """Explicitly register a model (fallback for legacy models)."""
    if hasattr(model_class, "__tablename__") and model_class.__tablename__:
        _models[model_class.__tablename__] = model_class
        register_migrate_model(model_class)


def get_registered_models() -> dict[str, type]:
    """Return ``{tablename: model_class}`` for all discovered models."""
    return dict(_models)
