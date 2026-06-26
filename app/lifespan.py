from collections.abc import AsyncIterator
from contextlib import asynccontextmanager
import logging

from fastapi import FastAPI

from app.core.security.permission_registry import sync_permission_registry
from app.platform.cache.redis import close_redis, init_redis
from app.platform.db.session import close_engine, init_engine
from app.platform.http.client import close_http_client, init_http_client
from app.platform.observability.tracing import shutdown_tracing

logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    init_engine()
    await init_redis()
    try:
        await sync_permission_registry(app)
    except Exception:
        logger.exception("Permission registry sync failed during startup")
    await init_http_client()
    try:
        yield
    finally:
        await close_http_client()
        await close_redis()
        await close_engine()
        shutdown_tracing()
