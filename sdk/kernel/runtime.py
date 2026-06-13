from __future__ import annotations

import logging
from dataclasses import dataclass

from fastapi import FastAPI

from sdk.config.settings import settings
from sdk.kernel.plugin import discover_and_load, init_plugins, plugin_snapshot, start_plugins, stop_plugins
from sdk.kernel.plugin.core_plugins import set_current_app
from sdk.kernel.registry import execute_middlewares, execute_routes, freeze, snapshot_state
from sdk.web.middleware import AuthMiddleware, MetricsMiddleware, TraceMiddleware, setup_cors, setup_exception_handlers

logger = logging.getLogger(__name__)


@dataclass
class RuntimeSnapshot:
    plugins: list[dict]
    routes: list[str]
    middlewares: list[str]
    permissions: list[str]
    models: list[str]


class ApplicationRuntime:
    def __init__(self) -> None:
        self._app: FastAPI | None = None
        self._assembled = False

    def build_app(self) -> FastAPI:
        self.assemble()

        app = FastAPI(
            title=settings.app.name,
            version=settings.app.version,
            docs_url="/docs" if settings.swagger.enabled else None,
            redoc_url="/redoc" if settings.swagger.enabled else None,
            openapi_url="/openapi.json" if settings.swagger.enabled else None,
        )
        set_current_app(app)
        app.add_middleware(MetricsMiddleware)
        app.add_middleware(TraceMiddleware)
        app.add_middleware(AuthMiddleware)
        setup_cors(app)
        setup_exception_handlers(app)
        execute_middlewares(app)
        execute_routes(app)
        self._app = app
        return app

    def assemble(self) -> None:
        if self._assembled:
            return
        discover_and_load()
        init_plugins()
        freeze()
        self._assembled = True
        self._log_assembly_summary()

    def snapshot(self) -> RuntimeSnapshot:
        reg = snapshot_state()
        return RuntimeSnapshot(
            plugins=plugin_snapshot(),
            routes=reg.routes,
            middlewares=reg.middlewares,
            permissions=reg.permissions,
            models=reg.models,
        )

    async def startup(self) -> None:
        await start_plugins(self._app)

    async def shutdown(self) -> None:
        await stop_plugins(self._app)

    def _log_assembly_summary(self) -> None:
        snapshot = self.snapshot()
        logger.info(
            "[APP] Assembly frozen: plugins=%d routes=%d middlewares=%d models=%d permissions=%d",
            len(snapshot.plugins),
            len(snapshot.routes),
            len(snapshot.middlewares),
            len(snapshot.models),
            len(snapshot.permissions),
        )


runtime = ApplicationRuntime()
