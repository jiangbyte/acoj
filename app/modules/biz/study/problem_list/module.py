from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="biz.study.problem_list",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.biz.study.problem_list.router:admin_router",
        ),
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.biz.study.problem_list.router:portal_router",
        ),
    ),
    order=220,
    depends_on=("biz.problem.problem", "biz.submission.submission"),
    models=("app.modules.biz.study.problem_list.model",),
)
