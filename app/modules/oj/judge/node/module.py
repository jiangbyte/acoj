from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.judge.node",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.judge.node.router:router",
        ),
    ),
    models=("app.modules.oj.judge.node.model",),
)
