from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.judge.language",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.judge.language.router:router",
        ),
    ),
    models=("app.modules.oj.judge.language.model",),
)
