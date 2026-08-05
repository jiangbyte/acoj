from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="biz.course",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.biz.course.router:admin_router",
        ),
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.biz.course.router:portal_router",
        ),
    ),
    order=231,
    depends_on=("biz.clazz", "biz.problem.problem", "biz.submission.submission"),
    models=("app.modules.biz.course.model",),
)
