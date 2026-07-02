from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="iam.permission",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.iam.permission.router:router",
        ),
    ),
)
