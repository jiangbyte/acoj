from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.judge.task",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.judge.task.router:router",
        ),
    ),
    models=("app.modules.oj.judge.task.model",),
)
