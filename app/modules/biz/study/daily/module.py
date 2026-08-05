from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="biz.study.daily",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.biz.study.daily.router:admin_router",
        ),
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.biz.study.daily.router:portal_router",
        ),
    ),
    order=222,
    depends_on=("biz.problem.problem", "biz.submission.submission"),
    models=("app.modules.biz.study.daily.model",),
)
