from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="sys.codegen",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.sys.codegen.router:router",
        ),
    ),
    models=("app.modules.sys.codegen.model",),
)
