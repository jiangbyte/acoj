from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.submission.case",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.submission.case.router:router",
        ),
    ),
    models=("app.modules.oj.submission.case.model",),
)
