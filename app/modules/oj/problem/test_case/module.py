from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.problem.test_case",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.problem.test_case.router:router",
        ),
    ),
    models=("app.modules.oj.problem.test_case.model",),
)
