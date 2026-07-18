from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.submission.rejudge_record",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.submission.rejudge_record.router:router",
        ),
    ),
    models=("app.modules.oj.submission.rejudge_record.model",),
)
