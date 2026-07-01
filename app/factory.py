from fastapi import FastAPI

from app.core.schema.health import RootHealthResponse
from app.core.config.settings import settings
from app.core.exceptions.handlers import customize_openapi_error_responses, register_exception_handlers
from app.core.logger.setup import setup_logging
from app.lifespan import lifespan
from app.middleware.access_log import AccessLogMiddleware
from app.middleware.auth_context import AuthContextMiddleware
from app.middleware.cors import add_cors
from app.middleware.trace import TraceMiddleware
from app.platform.db.session import engine
from app.platform.observability.manager import setup_observability
from app.api.router import router as api_router


def create_app() -> FastAPI:
    setup_logging()
    app = FastAPI(
        title=settings.app.name,
        debug=False,
        docs_url="/docs" if settings.swagger.enabled else None,
        redoc_url="/redoc" if settings.swagger.enabled else None,
        openapi_url="/openapi.json" if settings.swagger.enabled else None,
        lifespan=lifespan,
    )
    app.add_middleware(TraceMiddleware)
    app.add_middleware(AccessLogMiddleware)
    app.add_middleware(AuthContextMiddleware)
    add_cors(app)
    register_exception_handlers(app)
    customize_openapi_error_responses(app)
    setup_observability(app, engine=engine)

    @app.get("/", tags=["health"], response_model=RootHealthResponse)
    async def root() -> RootHealthResponse:
        return RootHealthResponse(status="ok", service=settings.app.name)

    app.include_router(api_router)
    return app
