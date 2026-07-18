from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.judge.runtime_version",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.judge.runtime_version.router:router",
        ),
    ),
    models=("app.modules.oj.judge.runtime_version.model",),
)
