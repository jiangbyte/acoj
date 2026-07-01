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
    uvicorn.run(
        "app.main:app",
        host=settings.app.host,
        port=settings.app.port,
        reload=settings.app.debug,
    )


if __name__ == "__main__":
    main()
