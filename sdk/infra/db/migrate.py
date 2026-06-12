"""
Migration and seed registries for hei-fastapi.

Mirrors hei-gin's ``sdk/db/migrate.go``:

- ``register_model(model)``
- ``get_models()``
- ``register_seed(name, fn)``
- ``run_seeds()``
"""

from __future__ import annotations

import logging
from collections.abc import Callable

logger = logging.getLogger(__name__)

_registered_models: dict[str, type] = {}


def register_model(model_class: type) -> None:
    """Register a SQLAlchemy model for migration discovery."""
    table_name = getattr(model_class, "__tablename__", "")
    if not table_name:
        return
    _registered_models[table_name] = model_class


def get_models() -> list[type]:
    """Return all registered models in registration order."""
    return list(_registered_models.values())


SeedFn = Callable[[], None]
_registered_seeds: list[tuple[str, SeedFn]] = []


def register_seed(name: str, fn: SeedFn) -> None:
    """Register an idempotent seed function."""
    _registered_seeds.append((name, fn))


def run_seeds() -> None:
    """Execute all registered seeds in registration order."""
    for name, fn in _registered_seeds:
        logger.info("[Seed] Running: %s", name)
        fn()
