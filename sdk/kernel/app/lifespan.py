from contextlib import asynccontextmanager

from fastapi import FastAPI

from sdk.config.settings import settings
from sdk.infra.db import dispose, redis_close, redis_init, verify_connection
from sdk.kernel.runtime import runtime


@asynccontextmanager
async def lifespan(app: FastAPI):
    settings.validate_runtime(redis_required=True)
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
            dispose()
        if shutdown_error is not None:
            raise shutdown_error
