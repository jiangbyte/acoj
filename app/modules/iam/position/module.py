from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="iam.position",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.iam.position.router:router",
        ),
    ),
    models=("app.modules.iam.position.model",),
)
