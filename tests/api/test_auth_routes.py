from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountStatusEnum, AccountType
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
            account_type=AccountType.PORTAL.value,
            account_status=AccountStatusEnum.ENABLED.value,
            name="Portal Login User",
            nickname="Portal Login User",
        )
        db_session.add(account)
        await db_session.commit()
        break

    response = await client.post(
        "/api/v1/portal/login",
        json={"account": "portal_login_user", "password": "Portal@123456"},
    )

    assert response.status_code == 200
    payload = response.json()
    assert payload["code"] == 200
    data = payload["data"]
    assert data["account_type"] == AccountType.PORTAL.value


async def test_logout_rejects_bearer_prefixed_token(client):
    override = client._transport.app.dependency_overrides[get_db_session]
    async for session in override():
        db_session: AsyncSession = session
        account = SysAccount(
            account="portal_logout_user",
            password_hash=hash_password("Portal@123456"),
            account_type=AccountType.PORTAL.value,
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
                account_type=AccountType.PORTAL.value,
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
        "/api/v1/portal/logout",
        headers={"Authorization": "Bearer portal-raw-token"},
    )

    assert response.status_code == 401
    assert response.json()["code"] == 401
    assert response.json()["message"] == "Invalid or expired token"


async def test_protected_admin_route_without_token_returns_401(client):
    response = await client.get("/api/v1/admin/file/page")

    assert response.status_code == 401
    assert response.json()["code"] == 401
    assert response.json()["message"] == "Missing authorization token"


async def test_protected_admin_route_with_invalid_token_returns_401(client):
    response = await client.get(
        "/api/v1/admin/file/page",
        headers={"Authorization": "invalid-admin-token"},
    )

    assert response.status_code == 401
    assert response.json()["code"] == 401
    assert response.json()["message"] == "Invalid or expired token"


async def test_admin_route_rejects_wrong_account_type_with_403(client):
    override = client._transport.app.dependency_overrides[get_db_session]
    async for session in override():
        db_session: AsyncSession = session
        account = SysAccount(
            account="portal_account_type_user",
            password_hash=hash_password("Portal@123456"),
            account_type=AccountType.PORTAL.value,
            account_status=AccountStatusEnum.ENABLED.value,
            name="Portal Account Type User",
            nickname="Portal Account Type User",
        )
        db_session.add(account)
        await db_session.flush()
        await session_store.set(
            SessionPayload(
                token="portal-account-type-token",
                account_id=account.id,
                account_type=AccountType.PORTAL.value,
                role_ids=[],
                dept_ids=[],
                group_ids=[],
                permission_keys=["file:file:page"],
                permission_grants=[],
            ),
            ttl_seconds=3600,
        )
        await db_session.commit()
        break

    response = await client.get(
        "/api/v1/admin/file/page",
        headers={"Authorization": "portal-account-type-token"},
    )

    assert response.status_code == 403
    assert response.json()["code"] == 403
    assert response.json()["message"] == "Account type 'PORTAL' is not allowed"


async def test_admin_route_rejects_missing_permission_with_403(client):
    override = client._transport.app.dependency_overrides[get_db_session]
    async for session in override():
        db_session: AsyncSession = session
        account = SysAccount(
            account="admin_without_permission",
            password_hash=hash_password("Admin@123456"),
            account_type=AccountType.ADMIN.value,
            account_status=AccountStatusEnum.ENABLED.value,
            name="Admin Without Permission",
            nickname="Admin Without Permission",
        )
        db_session.add(account)
        await db_session.flush()
        await session_store.set(
            SessionPayload(
                token="admin-without-permission-token",
                account_id=account.id,
                account_type=AccountType.ADMIN.value,
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

    response = await client.get(
        "/api/v1/admin/file/page",
        headers={"Authorization": "admin-without-permission-token"},
    )

    assert response.status_code == 403
    assert response.json()["code"] == 403
    assert response.json()["message"] == "Permission denied: file:file:page"


async def test_admin_route_allows_valid_account_type_and_permission(client):
    override = client._transport.app.dependency_overrides[get_db_session]
    async for session in override():
        db_session: AsyncSession = session
        account = SysAccount(
            account="admin_with_permission",
            password_hash=hash_password("Admin@123456"),
            account_type=AccountType.ADMIN.value,
            account_status=AccountStatusEnum.ENABLED.value,
            name="Admin With Permission",
            nickname="Admin With Permission",
        )
        db_session.add(account)
        await db_session.flush()
        await session_store.set(
            SessionPayload(
                token="admin-with-permission-token",
                account_id=account.id,
                account_type=AccountType.ADMIN.value,
                role_ids=[],
                dept_ids=[],
                group_ids=[],
                permission_keys=["file:file:page"],
                permission_grants=[],
            ),
            ttl_seconds=3600,
        )
        await db_session.commit()
        break

    response = await client.get(
        "/api/v1/admin/file/page",
        headers={"Authorization": "admin-with-permission-token"},
    )

    assert response.status_code == 200
    assert response.json()["code"] == 200


async def test_login_validation_error_uses_unified_response(client):
    response = await client.post(
        "/api/v1/portal/login",
        json={"account": "ab", "password": "123"},
    )

    assert response.status_code == 422
    assert response.json() == {
        "code": 422,
        "message": "account: String should have at least 3 characters",
        "data": None,
    }
