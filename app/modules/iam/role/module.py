from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="iam.role",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.iam.role.router:router",
        ),
    ),
    models=("app.modules.iam.role.model",),
)
