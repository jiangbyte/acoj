from __future__ import annotations

from fastapi import Request

import pytest

from sdk.auth.realm import Realm


class _Tool:
    @staticmethod
    def getTokenName() -> str:
        return "Authorization"

    @staticmethod
    async def getLoginIdDefaultNull(request=None):
        return "u1"

    @staticmethod
    async def getTokenTimeout(request=None):
        return 10

    @staticmethod
    async def getSessionTimeout(request=None):
        return 20

    @staticmethod
    async def renewTimeout(timeout=None, request=None):
        return None

    @staticmethod
    async def disable(login_id, time_seconds):
        return None

    @staticmethod
    async def isDisable(login_id):
        return True

    @staticmethod
    async def checkDisable(login_id):
        return None

    @staticmethod
    async def getDisableTime(login_id):
        return 30

    @staticmethod
    async def untieDisable(login_id):
        return None

    @staticmethod
    async def kickout(login_id):
        return None

    @staticmethod
    async def kickout_token(login_id, token):
        return None

    @staticmethod
    async def getClaims(request=None):
        return {
            "user_id": "u1",
            "realm_id": "BUSINESS",
            "created_at": "2024-01-01 00:00:00",
            "extra": {},
            "acl": {"permissions": [], "roles": [], "scope_map": {}},
        }, True


@pytest.mark.asyncio
async def test_realm_exposes_gin_compatible_helpers() -> None:
    realm = Realm("BUSINESS", _Tool)

    assert realm.get_token_name() == "Authorization"
    assert await realm.get_login_id_default_null(None) == "u1"
    assert await realm.get_token_timeout(None) == 10
    assert await realm.get_session_timeout(None) == 20
    assert await realm.get_disable_time("u1") == 30
    assert await realm.is_disable("u1") is True

    claims, ok = await realm.get_claims(None)
    assert ok is True
    assert claims is not None
    assert claims.user_id == "u1"
