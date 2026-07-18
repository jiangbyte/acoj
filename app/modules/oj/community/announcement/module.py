from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.community.announcement",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.community.announcement.router:router",
        ),
    ),
    models=("app.modules.oj.community.announcement.model",),
)
