from __future__ import annotations

from datetime import datetime, timezone

from fastapi import APIRouter, Response, status

from sdk.config.settings import settings
from sdk.infra.db import get_redis
from sdk.infra.db.mysql import engine
from sdk.kernel.plugin import plugins_ready
from sdk.infra.db import snapshot as migration_snapshot
from sdk.kernel.registry import snapshot_state
from sdk.observability import handler as metrics_handler
from sdk.log import log_persister_snapshot

router = APIRouter()


@router.get("/", summary="Health Check")
async def health_check():
    return {"message": f"{settings.app.name} is running", "version": settings.app.version}


@router.get("/health/live", summary="Liveness")
async def live_check():
    return {
        "status": "ok",
        "service": settings.app.name,
        "version": settings.app.version,
    }


@router.get("/health/ready", summary="Readiness")
async def ready_check(response: Response):
    ready, plugins = plugins_ready()
    components = await _readiness_components()
    ready = ready and all(item["ok"] for item in components)
    response.status_code = status.HTTP_200_OK if ready else status.HTTP_503_SERVICE_UNAVAILABLE
    return {
        "ready": ready,
        "service": settings.app.name,
        "version": settings.app.version,
        "env": settings.app.env,
        "loaded_env_files": settings.loaded_env_files,
        "checked_at": _checked_at(),
        "components": components,
        "plugins": plugins,
    }


@router.get("/debug/registry", summary="Registry Snapshot")
async def registry_snapshot():
    snapshot = snapshot_state()
    migrations = migration_snapshot()
    return {
        "service": settings.app.name,
        "version": settings.app.version,
        "checked_at": _checked_at(),
        "routes": snapshot.routes,
        "middlewares": snapshot.middlewares,
        "permissions": snapshot.permissions,
        "models": migrations.models,
        "seeds": migrations.seeds,
        "log_persister": log_persister_snapshot(),
        "frozen": snapshot.frozen and migrations.frozen,
    }


@router.get("/metrics", include_in_schema=False)
async def metrics():
    return metrics_handler()


def _checked_at() -> str:
    return datetime.now(timezone.utc).isoformat()


async def _readiness_components() -> list[dict[str, object]]:
    return [
        await _check_mysql(),
        await _check_redis(),
    ]


async def _check_mysql() -> dict[str, object]:
    try:
        with engine.connect() as conn:
            conn.exec_driver_sql("SELECT 1")
        return {"name": "mysql", "ok": True}
    except Exception as exc:
        return {"name": "mysql", "ok": False, "detail": str(exc)}


async def _check_redis() -> dict[str, object]:
    redis_client = get_redis()
    if redis_client is None:
        return {"name": "redis", "ok": False, "detail": "not initialized"}
    try:
        await redis_client.ping()
        return {"name": "redis", "ok": True}
    except Exception as exc:
        return {"name": "redis", "ok": False, "detail": str(exc)}
