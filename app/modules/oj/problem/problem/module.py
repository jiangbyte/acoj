from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.problem.problem",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.problem.problem.router:router",
        ),
    ),
    models=("app.modules.oj.problem.problem.model",),
)
