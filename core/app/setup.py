"""
Application factory — mirrors hei-gin's ``sdk/app/app.go Run()``.

Orchestration order (matching hei-gin):

  1. ``discover_and_load()``   —  import plugins (fires __init_subclass__ + module-level reg)
  2. ``init_plugins()``        —  plugin instances + on_init()
  3. Core middleware            —  Trace, Auth, CORS, Exception
  4. ``execute_middlewares()``  —  plugin-supplied middleware
  5. ``execute_routes()``      —  plugin-supplied routes
"""

import logging

from fastapi import FastAPI

from config.settings import settings
from .lifespan import lifespan
from core.middleware import setup_cors, setup_exception_handlers, AuthMiddleware, TraceMiddleware
from core.log.utils import TraceIdFilter


def create_app() -> FastAPI:
    logging.getLogger().addFilter(TraceIdFilter())

    app = FastAPI(
        title=settings.app.name,
        version=settings.app.version,
        lifespan=lifespan,
    )

    # ── 1. Plugin discovery + on_init() ─────────────────────────────
    from core.plugin import discover_and_load, init_plugins

    discover_and_load()
    init_plugins()

    # ── 2. Core middleware (always applied, outermost first) ─────────
    app.add_middleware(TraceMiddleware)
    app.add_middleware(AuthMiddleware)

    setup_cors(app)
    setup_exception_handlers(app)

    # ── 3. Plugin-supplied middleware ────────────────────────────────
    from core.plugin import execute_middlewares

    execute_middlewares(app)

    # ── 4. Plugin-supplied routes ────────────────────────────────────
    from core.plugin import execute_routes

    execute_routes(app)

    # ── 5. Static file mount for uploads ──────────────────────────────
    # Serves `/uploads/{bucket}/{file_key}` from the local upload folder.
    # Mirrors hei-gin where `/uploads/` is served by the web server / nginx.
    from fastapi.staticfiles import StaticFiles
    import os

    uploads_path = os.path.abspath(settings.storage.local.upload_folder)
    os.makedirs(uploads_path, exist_ok=True)
    app.mount("/uploads", StaticFiles(directory=uploads_path), name="uploads")

    return app

