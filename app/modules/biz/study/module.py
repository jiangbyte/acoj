from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="biz.study",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.biz.study.router:admin_router",
        ),
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.biz.study.router:portal_router",
        ),
    ),
    order=220,
    depends_on=("biz.problem.problem", "biz.submission.submission"),
    models=("app.modules.biz.study.model",),
)
