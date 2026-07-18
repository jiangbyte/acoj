from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.problem.objective_answer",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.problem.objective_answer.router:router",
        ),
    ),
    models=("app.modules.oj.problem.objective_answer.model",),
)
