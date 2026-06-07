"""
Plugin discovery and lifecycle orchestration.

Mirrors hei-gin's ``sdk/module/module.go``.

The ``start_plugins(app)`` and ``stop_plugins(app)`` functions accept
the FastAPI app instance so that plugins can access app routes etc.
"""

from __future__ import annotations

import logging
from typing import TYPE_CHECKING

from .interface import (
    HeiPlugin,
    get_registered_plugins,
    get_plugin_instances,
    instantiate_plugins,
)

if TYPE_CHECKING:
    from fastapi import FastAPI

logger = logging.getLogger(__name__)


def discover_and_load() -> None:
    """Phase 1: Import the ``plugins`` package and ``core_plugins``
    so that all ``__init_subclass__`` auto-registrations fire.

    Mirrors hei-gin's blank imports.
    """
    import plugins  # noqa: F401

    registered = get_registered_plugins()
    logger.info("Discovered %d plugin(s): %s", len(registered),
                [p.info().name for p in registered])


def init_plugins() -> None:
    """Phase 2: Instantiate plugins and call ``on_init()`` on each.

    Mirrors hei-gin's ``module.InitAll()``.
    """
    instantiate_plugins()
    for instance in get_plugin_instances():
        info = instance.info()
        logger.info("[Plugin] init: %s v%s", info.name, info.version)
        try:
            instance.on_init()
        except Exception:
            logger.exception("Plugin %s on_init() failed", info.name)
            raise


async def start_plugins(app: FastAPI | None = None) -> None:
    """Phase 3: Call ``on_start()`` on every plugin instance.

    The FastAPI ``app`` is forwarded so plugins can inspect routes, etc.
    Mirrors hei-gin's ``module.StartAll()``.
    """
    for instance in get_plugin_instances():
        info = instance.info()
        logger.info("[Plugin] start: %s v%s", info.name, info.version)
        try:
            await instance.on_start()
        except Exception:
            logger.exception("Plugin %s on_start() failed", info.name)
            raise


async def stop_plugins(app: FastAPI | None = None) -> None:
    """Phase 4: Call ``on_stop()`` in reverse registration order.

    Mirrors hei-gin's ``module.StopAll()``.
    """
    for instance in reversed(get_plugin_instances()):
        info = instance.info()
        logger.info("[Plugin] stop: %s v%s", info.name, info.version)
        try:
            await instance.on_stop()
        except Exception:
            logger.exception("Plugin %s on_stop() failed", info.name)
            raise
