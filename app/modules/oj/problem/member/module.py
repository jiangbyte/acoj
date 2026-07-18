from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.problem.member",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.problem.member.router:router",
        ),
    ),
    models=("app.modules.oj.problem.member.model",),
)
