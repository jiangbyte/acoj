from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="internal.health",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/internal",
            tags=("internal",),
            router="app.modules.internal.health.router:router",
            order=10,
        ),
    ),
)
