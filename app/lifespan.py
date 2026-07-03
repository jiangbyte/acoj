from collections.abc import AsyncIterator
from contextlib import asynccontextmanager
import logging

from fastapi import FastAPI

from app.core.security.permission_registry import sync_permission_registry
from app.modules.sys.audit.queue import start_operation_audit_queue, stop_operation_audit_queue
from app.platform.cache.redis import close_redis, init_redis
from app.platform.db.session import close_engine, init_engine
from app.platform.http.client import close_http_client, init_http_client
from app.platform.module import (
    load_module_specs,
    run_shutdown_hooks,
    run_startup_hooks,
)
from app.platform.observability.tracing import shutdown_tracing
from app.platform.tasks.autostart import celery_process_manager

logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    logger.info("lifespan startup: app.routes count = %d", len(app.routes))
    module_specs = load_module_specs()
    init_engine()
    await init_redis()
    await start_operation_audit_queue()
    await sync_permission_registry(app)
    await init_http_client()
    await run_startup_hooks(module_specs)
    celery_process_manager.start()
    try:
        yield
    finally:
        await celery_process_manager.stop()
        await run_shutdown_hooks(module_specs)
        await stop_operation_audit_queue()
        await close_http_client()
        await close_redis()
        await close_engine()
        shutdown_tracing()
