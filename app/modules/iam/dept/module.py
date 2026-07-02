from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="iam.dept",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.iam.dept.router:router",
        ),
    ),
    models=("app.modules.iam.dept.model",),
)
