import logging

from fastapi import FastAPI

from sdk.kernel.runtime import runtime
from .lifespan import lifespan


def create_app() -> FastAPI:
    from sdk.log.utils import TraceIdFilter

    logging.getLogger().addFilter(TraceIdFilter())

    runtime.assemble()
    app = runtime.build_app()
    app.router.lifespan_context = lifespan
    return app
