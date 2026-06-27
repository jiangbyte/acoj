from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountStatusEnum, LoginScope, UserType
from app.core.security.password import hash_password
from app.core.security.session import SessionPayload, session_store
from app.deps.db import get_db_session
from app.modules.iam.account.model import SysAccount


async def test_public_auth_login_route_not_found(client):
    response = await client.post(
        "/api/v1/public/auth/login",
        json={"account": "anyone", "password": "Secret@123"},
    )
    assert response.status_code == 404
    assert response.json() == {"code": 404, "message": "Not Found", "data": None}


async def test_portal_auth_login_success(client):
    override = client._transport.app.dependency_overrides[get_db_session]
    async for session in override():
        db_session: AsyncSession = session
        account = SysAccount(
            account="portal_login_user",
            password_hash=hash_password("Portal@123456"),
            account_type=UserType.PORTAL.value,
            account_status=AccountStatusEnum.ENABLED.value,
            name="Portal Login User",
            nickname="Portal Login User",
        )
        db_session.add(account)
        await db_session.commit()
        break

    response = await client.post(
        "/api/v1/portal/auth/login",
        json={"account": "portal_login_user", "password": "Portal@123456"},
    )

    assert response.status_code == 200
    payload = response.json()
    assert payload["code"] == 200
    data = payload["data"]
    assert data["account_type"] == UserType.PORTAL.value
    assert data["login_scope"] == LoginScope.PORTAL.value


async def test_logout_rejects_bearer_prefixed_token(client):
    override = client._transport.app.dependency_overrides[get_db_session]
    async for session in override():
        db_session: AsyncSession = session
        account = SysAccount(
            account="portal_logout_user",
            password_hash=hash_password("Portal@123456"),
            account_type=UserType.PORTAL.value,
            account_status=AccountStatusEnum.ENABLED.value,
            name="Portal Logout User",
            nickname="Portal Logout User",
        )
        db_session.add(account)
        await db_session.flush()
        await session_store.set(
            SessionPayload(
                token="portal-raw-token",
                account_id=account.id,
                account_type=UserType.PORTAL.value,
                login_scope=LoginScope.PORTAL.value,
                role_ids=[],
                dept_ids=[],
                group_ids=[],
                permission_keys=[],
                permission_grants=[],
            ),
            ttl_seconds=3600,
        )
        await db_session.commit()
        break

    response = await client.post(
        "/api/v1/portal/auth/logout",
        headers={"Authorization": "Bearer portal-raw-token"},
    )

    assert response.status_code == 401
    assert response.json()["code"] == 401
    assert response.json()["message"] == "Invalid or expired token"


async def test_login_validation_error_uses_unified_response(client):
    response = await client.post(
        "/api/v1/portal/auth/login",
        json={"account": "ab", "password": "123"},
    )

    assert response.status_code == 422
    assert response.json() == {
        "code": 422,
        "message": "account: String should have at least 3 characters",
        "data": None,
    }
