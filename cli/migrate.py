"""
Database migration tool for hei-fastapi.

Mirrors hei-gin's model + seed registration flow.

Usage::

    python -m cli.migrate                  # dry-run
    python -m cli.migrate --apply          # create missing tables
    python -m cli.migrate --seed           # run seeds only
    python -m cli.migrate --apply --seed   # migrate then seed
"""

from __future__ import annotations

import argparse
import logging

from sqlalchemy import inspect

from core.db import engine, get_models, run_seeds

logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")
logger = logging.getLogger("migrate")


def ensure_plugins_loaded() -> None:
    import plugins  # noqa: F401


def run_migration(*, dry_run: bool) -> list[str]:
    """Create missing tables from the registered model set."""
    inspector = inspect(engine)
    existing_tables = set(inspector.get_table_names())
    created_tables: list[str] = []

    models = get_models()
    if not models:
        logger.warning("No registered models found")
        return created_tables

    seen_tables: set[str] = set()
    for model in models:
        table = getattr(model, "__table__", None)
        if table is None or table.name in seen_tables:
            continue
        seen_tables.add(table.name)

        if table.name in existing_tables:
            logger.debug("Table exists: %s", table.name)
            continue

        if dry_run:
            logger.info("[dry-run] Would create table: %s", table.name)
        else:
            table.create(bind=engine)
            logger.info("Created table: %s", table.name)
        created_tables.append(table.name)

    if not created_tables:
        logger.info("Database is up-to-date (no new tables).")

    return created_tables


def main() -> None:
    parser = argparse.ArgumentParser(description="hei-fastapi database migration tool")
    parser.add_argument("--apply", action="store_true", help="Apply pending migrations")
    parser.add_argument("--seed", action="store_true", help="Run registered seed data")
    args = parser.parse_args()

    ensure_plugins_loaded()

    run_migration(dry_run=not args.apply)

    if args.seed:
        run_seeds()


if __name__ == "__main__":
    main()
