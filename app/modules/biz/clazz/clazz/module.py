from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="biz.clazz.clazz",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.biz.clazz.clazz.router:admin_router",
        ),
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.biz.clazz.clazz.router:portal_router",
        ),
    ),
    order=230,
    depends_on=("message.group",),
    models=("app.modules.biz.clazz.clazz.model",),
)
