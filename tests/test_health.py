from __future__ import annotations

from fastapi import Response

import pytest

from sdk.kernel.app import health


@pytest.mark.asyncio
async def test_ready_check_returns_503_when_dependencies_unready(monkeypatch) -> None:
    monkeypatch.setattr(health, "plugins_ready", lambda: (False, [{"name": "auth", "start_ok": False}]))
    monkeypatch.setattr(health, "_readiness_components", lambda: _ready_components(False))

    response = Response()
    payload = await health.ready_check(response)

    assert response.status_code == 503
    assert payload["ready"] is False
    assert len(payload["components"]) == 2


async def _ready_components(ok: bool) -> list[dict[str, object]]:
    return [
        {"name": "mysql", "ok": ok},
        {"name": "redis", "ok": ok},
    ]
