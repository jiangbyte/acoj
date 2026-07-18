from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.community.clarification",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.community.clarification.router:router",
        ),
    ),
    models=("app.modules.oj.community.clarification.model",),
)
