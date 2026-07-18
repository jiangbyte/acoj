from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.problem.tag_relation",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.problem.tag_relation.router:router",
        ),
    ),
    models=("app.modules.oj.problem.tag_relation.model",),
)
