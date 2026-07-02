from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="auth",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.auth.router:admin_router",
            order=10,
        ),
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.auth.router:portal_router",
            order=10,
        ),
    ),
)
