import os
import sys
from pathlib import Path

if __package__ in {None, ""}:
    app_dir = Path(__file__).resolve().parent
    project_root = app_dir.parent
    sys.path = [path for path in sys.path if Path(path or ".").resolve() != app_dir]
    sys.path.insert(0, str(project_root))

import uvicorn

from app.core.config.settings import settings
from app.factory import create_app

app = create_app()


def main() -> None:
    workers = _resolve_workers()
    uvicorn.run(
        "app.main:app",
        host=settings.app.host,
        port=settings.app.port,
        reload=settings.app.debug,
        workers=None if settings.app.debug else workers,
    )


def _resolve_workers() -> int:
    if settings.app.debug:
        return 1
    if settings.app.workers > 0:
        return settings.app.workers
    cpu_count = os.cpu_count() or 1
    return max(1, min(cpu_count, settings.app.worker_max))


if __name__ == "__main__":
    main()
