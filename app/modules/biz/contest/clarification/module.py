from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="biz.contest.clarification",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.biz.contest.clarification.router:router",
        ),
    ),
    order=340,
    depends_on=("biz.contest.contest",),
    models=("app.modules.biz.contest.clarification.model",),
)
