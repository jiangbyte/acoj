from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.community.solution",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.community.solution.router:router",
        ),
    ),
    models=("app.modules.oj.community.solution.model",),
)
