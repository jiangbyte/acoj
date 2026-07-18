from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.submission.source",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.submission.source.router:router",
        ),
    ),
    models=("app.modules.oj.submission.source.model",),
)
