from app.platform.module import BeatScheduleSpec, ModuleSpec, RouteSpec

module = ModuleSpec(
    name="iam.account",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.iam.account.router:router",
        ),
    ),
    models=("app.modules.iam.account.model",),
    tasks=("app.modules.iam.account.tasks",),
    beat_schedules=(
        BeatScheduleSpec(
            name="purge-cancelled-accounts-daily",
            task="account.purge_cancelled_accounts",
            schedule=86400.0,
        ),
    ),
)
