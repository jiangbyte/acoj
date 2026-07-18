from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="sys.config",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.sys.config.router:router",
        ),
    ),
    models=("app.modules.sys.config.model",),
)
