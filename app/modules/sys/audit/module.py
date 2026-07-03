from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="sys.audit",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.sys.audit.router:router",
        ),
    ),
    models=("app.modules.sys.audit.model",),
)
