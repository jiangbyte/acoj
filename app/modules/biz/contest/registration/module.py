from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="biz.contest.registration",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.biz.contest.registration.router:router",
        ),
    ),
    order=341,
    depends_on=("biz.contest.contest",),
    models=("app.modules.biz.contest.registration.model",),
)
