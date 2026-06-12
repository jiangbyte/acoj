"""
Global registries mirroring hei-gin's ``sdk/kernel/registry`` package.
"""

from __future__ import annotations

import logging
from typing import Callable

from fastapi import APIRouter, FastAPI
from sqlalchemy.orm import DeclarativeBase

from sdk.auth.decorator import HeiCheckPermission
from sdk.enums import LoginTypeEnum
from sdk.infra.db.migrate import register_model as register_migrate_model

logger = logging.getLogger(__name__)

_route_registrars: list[Callable[[FastAPI], None]] = []
_middleware_registrars: list[Callable[[FastAPI], None]] = []
_perm_entries: dict[str, dict[str, str]] = {}
_models: dict[str, type] = {}


def register_route(fn: Callable[[FastAPI], None]) -> None:
    _route_registrars.append(fn)


def register_router(router: APIRouter) -> None:
    def _registrar(app: FastAPI) -> None:
        app.include_router(router)

    _route_registrars.append(_registrar)


def execute_routes(app: FastAPI) -> None:
    for fn in _route_registrars:
        fn(app)


def register_middleware(fn: Callable[[FastAPI], None]) -> None:
    _middleware_registrars.append(fn)


def execute_middlewares(app: FastAPI) -> None:
    for fn in _middleware_registrars:
        fn(app)


def _module_from_code(code: str) -> str:
    parts = code.split(":")
    return ":".join(parts[:-1]) if len(parts) > 1 else code


def Perm(code: str, name: str = "", login_type: str = LoginTypeEnum.BUSINESS) -> Callable:
    if code not in _perm_entries:
        _perm_entries[code] = {
            "code": code,
            "name": name,
            "module": _module_from_code(code),
        }
        logger.debug("Perm registered: %s (%s)", code, name)

    return HeiCheckPermission(code, login_type=login_type)


def ClientPerm(code: str, name: str = "") -> Callable:
    return Perm(code, name, login_type=LoginTypeEnum.CONSUMER)


def get_registered_perm_entries() -> dict[str, dict[str, str]]:
    return dict(_perm_entries)


class HeiBase(DeclarativeBase):
    def __init_subclass__(cls, **kwargs):
        super().__init_subclass__(**kwargs)
        if hasattr(cls, "__tablename__") and cls.__tablename__:
            _models[cls.__tablename__] = cls
            register_migrate_model(cls)
            logger.debug("Model auto-registered: %s (%s)", cls.__name__, cls.__tablename__)


def register_model(model_class: type) -> None:
    if hasattr(model_class, "__tablename__") and model_class.__tablename__:
        _models[model_class.__tablename__] = model_class
        register_migrate_model(model_class)


def get_registered_models() -> dict[str, type]:
    return dict(_models)
