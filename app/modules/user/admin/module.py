from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="user.admin",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.user.admin.router:router",
        ),
    ),
    models=("app.modules.user.admin.model",),
)
