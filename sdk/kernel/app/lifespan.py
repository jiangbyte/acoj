"""
Application lifespan — mirrors hei-gin's ``sdk/app/app.go``.

Simplified: core services (auth, SM2, captcha, scheduler) are now
independent ``HeiPlugin`` instances that manage their own lifecycle.

Lifespan only handles:
  1. DB + Redis connectivity (fail fast)
  2. Plugin lifecycle via ``start_plugins(app)`` / ``stop_plugins()``
"""

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from sdk.infra.db import verify_connection, redis_init, dispose, redis_close

logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # =================================================================
    # STARTUP
    # =================================================================
    await verify_connection()
    await redis_init()

    from sdk.kernel.plugin import start_plugins
    from sdk.kernel.plugin.core_plugins import set_current_app

    set_current_app(app)
    await start_plugins()

    logger.info("Application startup complete")
    yield

    # =================================================================
    # SHUTDOWN
    # =================================================================
    from sdk.kernel.plugin import stop_plugins
    await stop_plugins()

    await redis_close()
    dispose()
    logger.info("Application shutdown complete")
