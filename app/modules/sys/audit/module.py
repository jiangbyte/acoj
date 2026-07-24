from app.platform.module import BeatScheduleSpec, ModuleSpec, RouteSpec

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
    models=(
        "app.modules.sys.audit.model",
        "app.modules.sys.audit.alert_model",
    ),
    tasks=("app.modules.sys.audit.tasks",),
    beat_schedules=(
        BeatScheduleSpec(
            name="audit-analysis-cycle",
            task="audit.analysis_cycle",
            schedule=300.0,
        ),
    ),
)
