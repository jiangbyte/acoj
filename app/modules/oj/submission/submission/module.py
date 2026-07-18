from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.submission.submission",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.submission.submission.router:router",
        ),
    ),
    models=("app.modules.oj.submission.submission.model",),
)
