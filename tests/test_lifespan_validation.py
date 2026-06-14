from __future__ import annotations

import pytest
from fastapi import FastAPI
from importlib import import_module

lifespan_module = import_module("sdk.kernel.app.lifespan")


@pytest.mark.asyncio
async def test_lifespan_fails_fast_on_invalid_production_config(monkeypatch) -> None:
    class _Settings:
        class app:
            threadpool_tokens = 0

        def validate_runtime(self, redis_required: bool = True) -> None:
            raise ValueError("invalid production config: db.password must not use a default/weak value")

        def production_warnings(self) -> list[str]:
            return []

    monkeypatch.setattr(lifespan_module, "settings", _Settings())

    app = FastAPI()

    with pytest.raises(ValueError, match="invalid production config"):
        async with lifespan_module.lifespan(app):
            pass
