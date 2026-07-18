from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.contest.problem_result",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.contest.problem_result.router:router",
        ),
    ),
    models=("app.modules.oj.contest.problem_result.model",),
)
