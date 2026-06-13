import logging
import os

from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles

from sdk.config.settings import settings
from sdk.kernel.runtime import runtime
from .lifespan import lifespan


def create_app() -> FastAPI:
    from sdk.log.utils import TraceIdFilter

    logging.getLogger().addFilter(TraceIdFilter())

    runtime.assemble()
    app = runtime.build_app()
    app.router.lifespan_context = lifespan

    uploads_path = os.path.abspath(settings.storage.local.upload_folder)
    os.makedirs(uploads_path, exist_ok=True)
    app.mount("/uploads", StaticFiles(directory=uploads_path), name="uploads")
    return app
