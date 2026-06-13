"""
Code generation scaffolding tool for hei-fastapi plugins.

Usage::

    python -m cli.codegen list                  # list all plugins
    python -m cli.codegen scaffold plugin_xxx   # create new plugin scaffold
"""

from __future__ import annotations

import argparse
import logging
import re
from pathlib import Path

logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")
logger = logging.getLogger("codegen")

PLUGINS_DIR = Path(__file__).resolve().parent.parent / "plugins"
PLUGIN_NAME_RE = re.compile(r"^plugin_[a-z][a-z0-9_]*$")


PLUGIN_SCAFFOLD = {
    "__init__.py": """
\"\"\"
{name} plugin.
\"\"\"

from .assembly import register

__all__ = ["register"]

""",
    "plugin.py": """
\"\"\"
{PluginCls} — plugin definition.
\"\"\"

import logging
from sdk.kernel.plugin import HeiPlugin, PluginInfo
from .migrate import register_all_models

logger = logging.getLogger(__name__)


class {PluginCls}(HeiPlugin):
    @classmethod
    def info(cls) -> PluginInfo:
        return PluginInfo(name="{name}", version="1.0.0", description="{name} plugin")

    def on_init(self):
        register_all_models()
        logger.info("[{PluginCls}] Models registered")

    async def on_start(self):
        logger.info("[{PluginCls}] Started")

    async def on_stop(self):
        logger.info("[{PluginCls}] Stopped")

""",
    "assembly.py": """
from __future__ import annotations

from sdk.kernel.plugin.loader import register_plugin_class
from sdk.kernel.registry import register_router

from .api.v1.api import router
from .plugin import {PluginCls}


_registered = False


def register() -> None:
    global _registered
    if _registered:
        return

    register_plugin_class({PluginCls})
    register_router(router)
    _registered = True

""",
    "migrate.py": """
\"\"\"
Centralized migration registration for {name}.
\"\"\"

from __future__ import annotations

from sdk.infra.db import register_model

from .models import SampleModel


def register_all_models() -> None:
    register_model(SampleModel)


__all__ = ["register_all_models"]

""",
    "models.py": """
\"\"\"
{PluginCls} — ORM models.
\"\"\"

from sdk.kernel.registry import HeiBase
from sqlalchemy import DateTime, String
from sqlalchemy.orm import Mapped, mapped_column
from datetime import datetime


class SampleModel(HeiBase):
    __tablename__ = "{table_name}"
    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    name: Mapped[str] = mapped_column(String(128), default="")
    created_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)

""",
    "params.py": """
\"\"\"
{PluginCls} — request/response parameters.
\"\"\"

from typing import Optional
from pydantic import BaseModel


class SampleVO(BaseModel):
    id: str = ""
    name: str = ""

""",
    "service.py": """
\"\"\"
{PluginCls} — business logic.
\"\"\"

from typing import Optional

from fastapi import Depends
from sqlalchemy.orm import Session

from sdk.infra.db import get_db

from .repository import SampleRepository


class SampleService:
    def __init__(self, repository: SampleRepository):
        self.repository = repository

    @classmethod
    def from_db(cls, db: Session) -> "SampleService":
        return cls(SampleRepository(db))


def get_sample_service(db: Session = Depends(get_db)) -> SampleService:
    return SampleService.from_db(db)


""",
    "repository.py": """
\"\"\"
{PluginCls} — repository layer.
\"\"\"

from typing import Optional

from sqlalchemy.orm import Session
from sqlalchemy import select

from .models import SampleModel


class SampleRepository:
    def __init__(self, db: Session):
        self.db = db

    def find_by_id(self, id: str) -> Optional[SampleModel]:
        return self.db.execute(select(SampleModel).where(SampleModel.id == id)).scalar_one_or_none()

""",
}


def cmd_list():
    """List all discovered plugins."""
    if not PLUGINS_DIR.exists():
        logger.info("No plugins directory found.")
        return

    plugins = sorted(
        d.name for d in PLUGINS_DIR.iterdir()
        if d.is_dir() and not d.name.startswith("_") and d.name != "__pycache__"
    )
    if plugins:
        logger.info("Discovered plugins:")
        for p in plugins:
            has_plugin = (PLUGINS_DIR / p / "plugin.py").exists()
            logger.info("  %s  %s", p, "[plugin.py]" if has_plugin else "[no plugin.py]")
    else:
        logger.info("No plugins found.")


def cmd_scaffold(name: str):
    """Create a new plugin scaffold directory."""
    if not PLUGIN_NAME_RE.match(name):
        logger.error("Invalid plugin name \"%s\". Expected pattern: plugin_xxx using lowercase letters, digits, underscores.", name)
        return

    target = PLUGINS_DIR / name
    if target.exists():
        logger.error("Plugin \"%s\" already exists at %s", name, target)
        return

    plugin_cls = "".join(part.capitalize() for part in name.split("_")) + "Plugin"
    table_name = name.replace("plugin_", "", 1) if name.startswith("plugin_") else name

    context = {"name": name, "PluginCls": plugin_cls, "table_name": table_name}

    # Create directories
    (target / "api" / "v1").mkdir(parents=True, exist_ok=True)

    for filename, template in PLUGIN_SCAFFOLD.items():
        (target / filename).write_text(template.format(**context), encoding="utf-8")

    # Create api/__init__.py and api/v1/__init__.py + api.py
    (target / "api" / "__init__.py").write_text("", encoding="utf-8")
    (target / "api" / "v1" / "__init__.py").write_text(
        "from . import api as v1_router\n\n__all__ = [\"v1_router\"]\n",
        encoding="utf-8",
    )
    (target / "api" / "v1" / "api.py").write_text(
        '"""\n{name} API routes.\n"""\n\nfrom fastapi import APIRouter, Depends, Query\n\n'
        'from sdk.web.result import Result, success\n'
        'from ...service import SampleService, get_sample_service\n\n'
        'router = APIRouter()\n\n\n'
        '@router.get("/api/v1/{route_name}/sample/detail", summary="Sample detail", response_model=Result)\n'
        'def detail(id: str = Query(...), service: SampleService = Depends(get_sample_service)):\n'
        '    entity = service.repository.find_by_id(id)\n'
        '    if entity is None:\n'
        '        return success(None)\n'
        '    return success({{"id": entity.id, "name": entity.name}})\n'.format(**context, route_name=table_name),
        encoding="utf-8",
    )

    logger.info("Created plugin scaffold: %s", name)
    for p in sorted(target.rglob("*.py")):
        logger.info("  %s", p.relative_to(PLUGINS_DIR))
    logger.info("It will be auto-discovered on next app startup; no framework code changes required.")


def main():
    parser = argparse.ArgumentParser(description="hei-fastapi code generation tool")
    sub = parser.add_subparsers(dest="command")
    sub.add_parser("list", help="list all plugins")
    scaffold = sub.add_parser("scaffold", help="create new plugin scaffold")
    scaffold.add_argument("name", help="plugin name (e.g. plugin_xxx)")

    args = parser.parse_args()
    if args.command == "list":
        cmd_list()
    elif args.command == "scaffold":
        cmd_scaffold(args.name)
    else:
        parser.print_help()


if __name__ == "__main__":
    main()
