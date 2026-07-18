from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="oj.community.favorite",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.community.favorite.router:router",
        ),
    ),
    models=("app.modules.oj.community.favorite.model",),
)
