from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="sys.dict",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.sys.dict.router:router",
        ),
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.sys.dict.portal.router:router",
        ),
    ),
    models=("app.modules.sys.dict.model",),
)
