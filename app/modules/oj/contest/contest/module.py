from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.contest.contest",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.contest.contest.router:router",
        ),
    ),
    models=("app.modules.oj.contest.contest.model",),
)
