from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="message.notification",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.message.notification.router:admin_router",
        ),
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.message.notification.router:portal_router",
        ),
    ),
    models=("app.modules.message.notification.model",),
)
