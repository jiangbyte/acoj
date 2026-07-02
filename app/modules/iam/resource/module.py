from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="iam.resource",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.iam.resource.router:router",
        ),
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.iam.resource.portal.router:router",
        ),
    ),
    models=("app.modules.iam.resource.model",),
)
