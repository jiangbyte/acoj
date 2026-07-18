from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="sys.file",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.sys.file.router:router",
        ),
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.sys.file.portal.router:router",
        ),
        RouteSpec(
            version="v1",
            tags=("public",),
            router="app.modules.sys.file.public_router:router",
            order=10,
        ),
    ),
    models=("app.modules.sys.file.model",),
)
