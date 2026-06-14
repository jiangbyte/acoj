from __future__ import annotations

import pytest

from plugins.plugin_client.auth.username import logic as client_logic
from plugins.plugin_sys.auth.username import logic as sys_logic


class _User:
    def __init__(self) -> None:
        self.id = "u1"
        self.username = "alice"
        self.nickname = "Alice"
        self.status = "ACTIVE"
        self.password = "hashed"


class _API:
    def __init__(self) -> None:
        self.recorded = []

    def get_login_user_info_by_username(self, username: str):
        return _User() if username == "alice" else None

    def record_login(self, user_id: str, request) -> None:
        self.recorded.append((user_id, request))

    def create_user(self, username: str, hashed_password: str) -> None:
        self.recorded.append((username, hashed_password))

    def get_username_by_id(self, user_id: str) -> str:
        return "alice"


class _Captcha:
    async def check_captcha(self, captcha_id: str, captcha_code: str) -> bool:
        return True


class _Request:
    def __init__(self) -> None:
        self.headers = {"User-Agent": "pytest"}
        self.state = type("State", (), {})()


class _Param:
    def __init__(self) -> None:
        self.username = "alice"
        self.password = "encrypted"
        self.captcha_id = "cid"
        self.captcha_code = "1234"
        self.device_id = "dev-1"


@pytest.mark.asyncio
async def test_sys_login_uses_blocking_provider_via_helper(monkeypatch) -> None:
    api = _API()
    sys_logic.init_auth(api)
    monkeypatch.setattr(sys_logic, "b_captcha", _Captcha())
    monkeypatch.setattr(sys_logic, "decrypt", lambda _: "plain")
    monkeypatch.setattr(sys_logic, "get_browser", lambda _: "pytest")
    monkeypatch.setattr(sys_logic.Business, "login", lambda request, login_id, extra: _async_value("token"))
    monkeypatch.setattr(sys_logic, "record_auth_log", lambda *args, **kwargs: None)
    monkeypatch.setattr(sys_logic.asyncio, "to_thread", lambda func, *args: _async_value(True))

    result = await sys_logic.do_login(_Param(), _Request())

    assert result.token == "token"
    assert api.recorded


@pytest.mark.asyncio
async def test_client_register_hashes_in_threadpool(monkeypatch) -> None:
    api = _API()
    client_logic.init_auth(api)
    monkeypatch.setattr(client_logic, "c_captcha", _Captcha())
    monkeypatch.setattr(client_logic, "decrypt", lambda _: "plain")
    monkeypatch.setattr(client_logic, "record_auth_log", lambda *args, **kwargs: None)
    monkeypatch.setattr(
        client_logic.asyncio,
        "to_thread",
        lambda func, *args: _async_value(b"hashed-password"),
    )

    result = await client_logic.do_register(_Param(), _Request())

    assert result.message == "注册成功"
    assert api.recorded[0] == ("alice", "hashed-password")


async def _async_value(value):
    return value
