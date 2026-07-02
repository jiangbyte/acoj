from collections.abc import AsyncIterator
from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.core.security.permission_registry import sync_permission_registry
from app.platform.cache.redis import close_redis, init_redis
from app.platform.db.session import close_engine, init_engine
from app.platform.http.client import close_http_client, init_http_client
from app.platform.observability.tracing import shutdown_tracing
from app.platform.tasks.autostart import celery_process_manager


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    init_engine()
    await init_redis()
    await sync_permission_registry(app)
    await init_http_client()
    celery_process_manager.start()
    try:
        yield
    finally:
        await celery_process_manager.stop()
        await close_http_client()
        await close_redis()
        await close_engine()
        shutdown_tracing()
