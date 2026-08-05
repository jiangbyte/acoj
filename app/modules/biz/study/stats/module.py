from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="biz.study.stats",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.biz.study.stats.router:portal_router",
        ),
    ),
    order=223,
    depends_on=("biz.study.daily", "biz.problem.problem", "biz.submission.submission"),
    models=(),
)
