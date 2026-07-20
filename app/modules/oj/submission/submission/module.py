from app.platform.module import ModuleSpec, RouteSpec
from app.modules.oj.submission.submission.judge_result_consumer import result_consumer


def _start_consumer():
    result_consumer.start()


def _stop_consumer():
    return result_consumer.stop_async()


module = ModuleSpec(
    name="oj.submission.submission",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin", "oj"),
            router="app.modules.oj.submission.submission.router:router",
        ),
        RouteSpec(
            version="v1",
            prefix="/portal",
            tags=("portal", "oj"),
            router="app.modules.oj.submission.submission.router_portal:router",
        ),
    ),
    models=("app.modules.oj.submission.submission.model",),
    startup_hooks=(f"{__name__}:_start_consumer",),
    shutdown_hooks=(f"{__name__}:_stop_consumer",),
)
