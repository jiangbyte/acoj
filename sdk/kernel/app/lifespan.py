from contextlib import asynccontextmanager
import logging

import anyio.to_thread
from fastapi import FastAPI

from sdk.config.settings import settings
from sdk.infra.db import (
    dispose,
    dispose_sync,
    redis_close,
    redis_init,
    verify_connection,
    verify_connection_sync,
)
from sdk.kernel.runtime import runtime

logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    settings.validate_runtime(redis_required=True)
    _configure_threadpool()
    for warning in settings.production_warnings():
        logger.warning("[ProductionConfig] %s", warning)
    await verify_connection_sync()
    await verify_connection()
    await redis_init()
    await runtime.startup()
    try:
        yield
    finally:
        shutdown_error: Exception | None = None
        try:
            await runtime.shutdown()
        except Exception as exc:
            shutdown_error = exc
        try:
            await redis_close()
        finally:
            try:
                await dispose()
            finally:
                dispose_sync()
        if shutdown_error is not None:
            raise shutdown_error


def _configure_threadpool() -> None:
    tokens = settings.app.threadpool_tokens
    if tokens <= 0:
        return
    limiter = anyio.to_thread.current_default_thread_limiter()
    limiter.total_tokens = tokens
    logger.info("[Runtime] AnyIO threadpool tokens set to %d", tokens)
