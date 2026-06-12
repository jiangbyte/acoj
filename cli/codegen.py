"""
Code generation scaffolding tool for hei-fastapi plugins.

Usage::

    python -m cli.codegen list                  # list all plugins
    python -m cli.codegen scaffold plugin_xxx   # create new plugin scaffold
"""

from __future__ import annotations

import argparse
import logging
import os
from pathlib import Path

logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")
logger = logging.getLogger("codegen")

PLUGINS_DIR = Path(__file__).resolve().parent.parent / "plugins"


PLUGIN_SCAFFOLD = {
    "__init__.py": """
\"\"\"
{name} plugin.
\"\"\"

from . import plugin as _plugin

__all__ = []

""",
    "plugin.py": """
\"\"\"
{PluginCls} — plugin definition.
\"\"\"

import logging
from sdk.kernel.plugin import HeiPlugin, PluginInfo, register_router
from .api.v1 import api as v1_router

logger = logging.getLogger(__name__)
register_router(v1_router)


class {PluginCls}(HeiPlugin):
    @classmethod
    def info(cls) -> PluginInfo:
        return PluginInfo(name="{name}", version="1.0.0", description="{name} plugin")

    def on_init(self):
        logger.info("[{PluginCls}] Initialised")

    async def on_start(self):
        logger.info("[{PluginCls}] Started")

    async def on_stop(self):
        logger.info("[{PluginCls}] Stopped")

""",
    "models.py": """
\"\"\"
{PluginCls} — ORM models.
\"\"\"

from sdk.kernel.registry import HeiBase
from sqlalchemy import String
from sqlalchemy.orm import Mapped, mapped_column


class SampleModel(HeiBase):
    __tablename__ = "{table_name}"
    id: Mapped[str] = mapped_column(String(32), primary_key=True)

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

import logging
from typing import Optional, List
from sqlalchemy.orm import Session

logger = logging.getLogger(__name__)

""",
    "repository.py": """
\"\"\"
{PluginCls} — repository layer.
\"\"\"

from typing import Optional, List
from sqlalchemy.orm import Session
from sqlalchemy import select, func

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
        '"""\n{name} API routes.\n"""\n\nfrom fastapi import APIRouter\n\nrouter = APIRouter()\n'.format(**context),
        encoding="utf-8",
    )

    # Register in plugins/__init__.py
    plugin_init = PLUGINS_DIR / "__init__.py"
    existing = plugin_init.read_text(encoding="utf-8").rstrip()
    if f"import plugins.{name}" not in existing:
        plugin_init.write_text(existing + f"\nimport plugins.{name}\n", encoding="utf-8")

    logger.info("Created plugin scaffold: %s", name)
    for p in sorted(target.rglob("*.py")):
        logger.info("  %s", p.relative_to(PLUGINS_DIR))


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
