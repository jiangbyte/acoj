from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="message.friend",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.message.friend.router:admin_router",
        ),
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.message.friend.router:portal_router",
        ),
    ),
    models=("app.modules.message.friend.model",),
)
