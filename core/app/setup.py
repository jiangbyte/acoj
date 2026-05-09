from fastapi import FastAPI

from config.settings import settings
from .lifespan import lifespan
from .router import setup_routers
from core.middleware import setup_cors, setup_exception_handlers, AuthMiddleware


def create_app() -> FastAPI:
    app = FastAPI(
        title=settings.app.name,
        description="Hei FastAPI Application",
        version=settings.app.version,
        lifespan=lifespan,
    )

    app.add_middleware(AuthMiddleware)

    setup_cors(app)
    setup_exception_handlers(app)
    setup_routers(app)

    return app


app = create_app()
