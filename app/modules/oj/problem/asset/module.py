from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.problem.asset",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.problem.asset.router:router",
        ),
    ),
    models=("app.modules.oj.problem.asset.model",),
)
