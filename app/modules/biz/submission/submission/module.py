from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="biz.submission.submission",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.biz.submission.submission.router:router",
        ),
    ),
    order=400,
    depends_on=("biz.problem.problem", "biz.contest.contest"),
    models=("app.modules.biz.submission.submission.model",),
    tasks=("app.modules.biz.submission.submission.tasks",),
)
