from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="dashboard",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.dashboard.router:router",
            order=5,
        ),
    ),
)
