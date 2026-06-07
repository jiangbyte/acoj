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


PLUGIN_SCAFFOLD = {
    "__init__.py": """\"\"\"
{name} plugin — auto-discovered by ``plugins/__init__.py``.
\"\"\"

from .plugin import {PluginCls}

__all__ = ["{PluginCls}"]
""",
    "plugin.py": """\"\"\"
{PluginCls} — plugin definition.
\"\"\"

import logging

from core.plugin import HeiPlugin, PluginInfo, register_router

logger = logging.getLogger(__name__)


class {PluginCls}(HeiPlugin):
    @classmethod
    def info(cls) -> PluginInfo:
        return PluginInfo(
            name="{name}",
            version="1.0.0",
            description="{name} plugin",
        )

    @classmethod
    def on_init(cls):
        logger.info("[{PluginCls}] Initialised")

    @classmethod
    async def on_start(cls):
        logger.info("[{PluginCls}] Started")

    @classmethod
    async def on_stop(cls):
        logger.info("[{PluginCls}] Stopped")
""",
    "models.py": """\"\"\"
{PluginCls} — ORM models.
\"\"\"

from core.plugin.registry import HeiBase
from sqlalchemy import String
from sqlalchemy.orm import Mapped, mapped_column


class SampleModel(HeiBase):
    __tablename__ = '{table_name}'

    id: Mapped[str] = mapped_column(String(32), primary_key=True)
""",
    "admin.py": """\"\"\"
{PluginCls} — seed data.
\"\"\"

import logging

logger = logging.getLogger(__name__)


def run_seed(db):
    \"\"\"Seed initial data for this plugin.\"\"\"
    logger.info("[{PluginCls}] No seed data defined")
""",
}


def cmd_scaffold(name: str):
    """Create a new plugin scaffold directory."""
    target = PLUGINS_DIR / name
    if target.exists():
        logger.error("Plugin '%s' already exists at %s", name, target)
        return

    plugin_cls = "".join(part.capitalize() for part in name.split("_")) + "Plugin"
    table_name = name.replace("plugin_", "", 1) if name.startswith("plugin_") else name

    context = {
        "name": name,
        "PluginCls": plugin_cls,
        "table_name": table_name,
    }

    target.mkdir(parents=True, exist_ok=True)

    for filename, template in PLUGIN_SCAFFOLD.items():
        content = template.format(**context)
        (target / filename).write_text(content, encoding="utf-8")
        logger.info("  Created %s/%s", name, filename)

    (
        PLUGINS_DIR / "__init__.py"
    ).write_text(
        (PLUGINS_DIR / "__init__.py").read_text(encoding="utf-8").rstrip()
        + f"\nimport plugins.{name}\n",
        encoding="utf-8",
    )

    logger.info("✓ Created plugin scaffold: %s", name)
    logger.info("  Next: add route handlers and register_router() to plugin.py")


def main():
    parser = argparse.ArgumentParser(description="hei-fastapi 代码生成工具")
    sub = parser.add_subparsers(dest="command")
    sub.add_parser("list", help="列出所有插件")
    scaffold = sub.add_parser("scaffold", help="创建新插件脚手架")
    scaffold.add_argument("name", help="插件名称（如 plugin_xxx）")

    args = parser.parse_args()
    if args.command == "list":
        cmd_list()
    elif args.command == "scaffold":
        cmd_scaffold(args.name)
    else:
        parser.print_help()


if __name__ == "__main__":
    main()
