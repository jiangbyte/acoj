from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="biz.submission.portal",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal",),
            router="app.modules.biz.submission.portal.router:router",
        ),
    ),
    order=405,
    depends_on=("biz.submission.submission",),
    models=(),
)
