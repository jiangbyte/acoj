from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="biz.team",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.biz.team.router:admin_router",
        ),
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.biz.team.router:portal_router",
        ),
    ),
    order=232,
    depends_on=("biz.clazz", "biz.course", "message.group"),
    models=("app.modules.biz.team.model",),
)
