from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="biz.problem.portal",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.biz.problem.portal.router:router",
        ),
    ),
    order=215,
    depends_on=("biz.problem.problem",),
    models=(),
)
