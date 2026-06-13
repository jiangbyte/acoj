from __future__ import annotations

import logging
from collections.abc import Callable
from dataclasses import dataclass, field

logger = logging.getLogger(__name__)

SeedFn = Callable[[], None]


@dataclass
class MigrationSnapshot:
    models: list[str] = field(default_factory=list)
    seeds: list[str] = field(default_factory=list)
    frozen: bool = False


class MigrationRegistry:
    def __init__(self) -> None:
        self._models: list[type] = []
        self._model_names: list[str] = []
        self._model_index: set[str] = set()
        self._seeds: list[tuple[str, SeedFn]] = []
        self._seed_index: set[str] = set()
        self._frozen = False

    def ensure_mutable(self) -> None:
        if self._frozen:
            raise RuntimeError("migration registry is frozen")

    def register_model(self, model_class: type) -> None:
        self.ensure_mutable()
        if model_class is None:
            raise RuntimeError("model register failed: nil model")

        table_name = getattr(model_class, "__tablename__", "")
        if not table_name:
            return

        model_name = _model_name(model_class)
        if model_name in self._model_index:
            raise RuntimeError(f"duplicate model: {model_name}")

        self._model_index.add(model_name)
        self._models.append(model_class)
        self._model_names.append(model_name)

    def get_models(self) -> list[type]:
        return list(self._models)

    def register_seed(self, name: str, fn: SeedFn) -> None:
        self.ensure_mutable()
        if not name:
            raise RuntimeError("seed register failed: empty seed name")
        if fn is None:
            raise RuntimeError(f"seed register failed: nil seed func, seed={name}")
        if name in self._seed_index:
            raise RuntimeError(f"duplicate seed: {name}")

        self._seed_index.add(name)
        self._seeds.append((name, fn))

    def run_seeds(self) -> None:
        self.freeze()
        for name, fn in list(self._seeds):
            logger.info("[Seed] Running: %s", name)
            fn()

    def freeze(self) -> None:
        self._frozen = True

    def snapshot(self) -> MigrationSnapshot:
        return MigrationSnapshot(
            models=list(self._model_names),
            seeds=[name for name, _ in self._seeds],
            frozen=self._frozen,
        )


def _model_name(model_class: type) -> str:
    return f"{model_class.__module__}.{model_class.__qualname__}"


_registry = MigrationRegistry()


def register_model(model_class: type) -> None:
    _registry.register_model(model_class)


def get_models() -> list[type]:
    return _registry.get_models()


def register_seed(name: str, fn: SeedFn) -> None:
    _registry.register_seed(name, fn)


def run_seeds() -> None:
    _registry.run_seeds()


def freeze() -> None:
    _registry.freeze()


def snapshot() -> MigrationSnapshot:
    return _registry.snapshot()


def reset_for_test() -> None:
    global _registry
    _registry = MigrationRegistry()
