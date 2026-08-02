from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="biz.contest.portal",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.biz.contest.portal.router:router",
        ),
    ),
    order=320,
    depends_on=("biz.contest.contest",),
    models=(),
)
