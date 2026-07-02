from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="message.realtime",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.message.realtime.router:admin_router",
        ),
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.message.realtime.router:portal_router",
        ),
    ),
)
