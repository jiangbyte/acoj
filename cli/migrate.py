"""
Database migration tool for hei-fastapi.

Creates database tables based on all registered ORM models.

Usage::

    python -m cli.migrate                  # dry-run (show pending)
    python -m cli.migrate --apply           # apply pending migrations
"""

from __future__ import annotations

import argparse
import logging
import sys

from sqlalchemy import create_engine, inspect

from config.settings import settings

logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")
logger = logging.getLogger("migrate")


def _collect_metadata():
    """Collect metadata from all loaded ``DeclarativeBase`` subclasses."""
    from sqlalchemy.orm import DeclarativeBase

    seen = set()
    metadata = None

    for mod_name, mod in list(sys.modules.items()):
        if mod_name.startswith("_") or mod is None:
            continue
        for name in dir(mod):
            obj = getattr(mod, name, None)
            if isinstance(obj, type) and issubclass(obj, DeclarativeBase) and obj is not DeclarativeBase:
                if obj not in seen:
                    seen.add(obj)
                    if metadata is None:
                        metadata = obj.metadata
                    else:
                        for tname, table in obj.metadata.tables.items():
                            if tname not in metadata.tables:
                                metadata._add_table(tname, table.schema, table)

    # Also ensure HeiBase models are included
    from core.plugin.registry import HeiBase, get_registered_models
    hei_models = get_registered_models()
    if hei_models:
        if metadata is None:
            metadata = HeiBase.metadata
        else:
            for tname, table in HeiBase.metadata.tables.items():
                if tname not in metadata.tables:
                    metadata._add_table(tname, table.schema, table)

    return metadata


def run_migration(dry_run: bool = False) -> list[str]:
    """Compare ORM metadata against the database and create missing tables."""
    engine = create_engine(settings.db.url, echo=False)
    inspector = inspect(engine)
    existing_tables = set(inspector.get_table_names())

    metadata = _collect_metadata()
    statements: list[str] = []

    if metadata is None:
        logger.warning("No ORM metadata found – have you imported your models?")
        engine.dispose()
        return statements

    for name, table in metadata.tables.items():
        if name not in existing_tables:
            if not dry_run:
                table.create(engine)
                logger.info("Created table: %s", name)
            else:
                logger.info("[dry-run] Would create table: %s", name)
        else:
            logger.debug("Table exists: %s", name)

    if not statements:
        logger.info("Database is up-to-date (no new tables).")

    engine.dispose()
    return statements


def main():
    parser = argparse.ArgumentParser(description="hei-fastapi database migration tool")
    parser.add_argument("--apply", action="store_true", help="Apply pending migrations")
    args = parser.parse_args()

    # Ensure models are loaded
    import plugins  # noqa: F401

    if args.apply:
        run_migration(dry_run=False)
    else:
        run_migration(dry_run=True)


if __name__ == "__main__":
    main()
