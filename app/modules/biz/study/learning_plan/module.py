from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="biz.study.learning_plan",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.biz.study.learning_plan.router:admin_router",
        ),
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.biz.study.learning_plan.router:portal_router",
        ),
    ),
    order=221,
    depends_on=("biz.study.problem_list", "biz.problem.problem", "biz.submission.submission"),
    models=("app.modules.biz.study.learning_plan.model",),
)
