from __future__ import annotations

import pytest

from plugins.plugin_client.user.params import UpdatePasswordParam
from plugins.plugin_client.user.service import ClientUserService


class _Entity:
    def __init__(self) -> None:
        self.id = "u1"
        self.password = "hashed"


class _Repo:
    def __init__(self) -> None:
        self.updated = []
        self.db = self
        self.entity = _Entity()

    def find_by_id(self, user_id: str):
        return self.entity if user_id == "u1" else None

    def update(self, entity, user_id=None):
        self.updated.append((entity.password, user_id))


class _Actor:
    user_id = "u1"


@pytest.mark.asyncio
async def test_update_password_uses_async_threadpool(monkeypatch) -> None:
    service = ClientUserService.__new__(ClientUserService)
    service.repository = _Repo()
    service.db = service.repository.db

    monkeypatch.setattr("plugins.plugin_client.user.service.decrypt", lambda value: value)

    async def _fake_to_thread(func, *args):
        result = func(*args)
        return result

    monkeypatch.setattr("plugins.plugin_client.user.service.asyncio.to_thread", _fake_to_thread)
    monkeypatch.setattr("plugins.plugin_client.user.service.bcrypt.checkpw", lambda current, hashed: True)
    monkeypatch.setattr(
        "plugins.plugin_client.user.service.bcrypt.hashpw",
        lambda new_password, salt: b"new-hash",
    )
    monkeypatch.setattr("plugins.plugin_client.user.service.bcrypt.gensalt", lambda: b"salt")

    await service.update_password(
        UpdatePasswordParam(current_password="old", new_password="new"),
        _Actor(),
    )

    assert service.repository.updated == [("new-hash", "u1")]
