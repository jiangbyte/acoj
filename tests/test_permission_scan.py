from __future__ import annotations

import json

import pytest

from sdk.auth.consts import PermissionCacheKey
from sdk.auth.permission_scan import permission_cache_ttl_seconds, sync_to_redis


class _FakeRedis:
    def __init__(self) -> None:
        self.calls: list[tuple] = []

    async def set(self, *args, **kwargs):
        self.calls.append(("set", args, kwargs))


@pytest.mark.asyncio
async def test_sync_to_redis_uses_ttl(monkeypatch) -> None:
    fake = _FakeRedis()

    monkeypatch.setattr("sdk.auth.permission_scan.get_client", lambda: fake, raising=False)
    monkeypatch.setattr("sdk.auth.permission_scan.settings", type("S", (), {"raw": {"permission_cache_ttl": 123}})())

    async def _fake_get_client():
        return None

    from sdk.infra.db import redis as redis_module

    monkeypatch.setattr(redis_module, "get_client", lambda: fake)

    await sync_to_redis({"sys:user:view": {"code": "sys:user:view", "module": "sys:user", "name": "view"}})

    assert len(fake.calls) == 1
    op, args, kwargs = fake.calls[0]
    assert op == "set"
    assert args[0] == PermissionCacheKey
    assert json.loads(args[1])["sys:user"]["sys:user:view"]["name"] == "view"
    assert int(kwargs["ex"].total_seconds()) == 123


def test_permission_cache_ttl_defaults_to_300(monkeypatch) -> None:
    monkeypatch.setattr("sdk.auth.permission_scan.settings", type("S", (), {"raw": {}})())
    assert permission_cache_ttl_seconds() == 300
