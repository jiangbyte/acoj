from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="message.todo",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.message.todo.router:admin_router",
        ),
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.message.todo.router:portal_router",
        ),
    ),
    models=("app.modules.message.todo.model",),
)
