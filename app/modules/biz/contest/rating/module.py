"""Rating settlement module."""

from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="biz.contest.rating",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.biz.contest.rating.router:router",
        ),
    ),
    order=350,
    depends_on=("biz.contest.contest",),
    models=("app.modules.biz.contest.rating.model",),
)
