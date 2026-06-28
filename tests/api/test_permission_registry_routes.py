from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountStatusEnum, AccountType
from app.core.security.password import hash_password
from app.core.security.session import SessionPayload, session_store
from app.deps.db import get_db_session
from app.modules.iam.account.model import SysAccount


async def test_permission_registry_route_lists_registry(client, monkeypatch):
    async def fake_list_permission_registry(self):
        return [
            {
                "permission_key": "iam:permission:list",
                "module": "iam",
                "source": "app.modules.iam.permission.router",
                "methods": ["GET"],
                "account_types": [AccountType.ADMIN.value],
                "routes": [
                    {
                        "path": "/api/v1/admin/permissions/registry",
                        "methods": ["GET"],
                        "account_types": [AccountType.ADMIN.value],
                    }
                ],
            }
        ]

    monkeypatch.setattr(
        "app.modules.iam.permission.service.PermissionService.list_permission_registry",
        fake_list_permission_registry,
    )

    override = client._transport.app.dependency_overrides[get_db_session]
    async for session in override():
        db_session: AsyncSession = session
        account = SysAccount(
            account="admin_registry_user",
            password_hash=hash_password("Admin@123456"),
            account_type=AccountType.ADMIN.value,
            account_status=AccountStatusEnum.ENABLED.value,
            name="Admin Registry User",
            nickname="Admin Registry User",
        )
        db_session.add(account)
        await db_session.flush()
        await session_store.set(
            SessionPayload(
                token="admin-registry-token",
                account_id=account.id,
                account_type=AccountType.ADMIN.value,
                role_ids=[],
                dept_ids=[],
                group_ids=[],
                permission_keys=["iam:permission:list"],
                permission_grants=[],
            ),
            ttl_seconds=3600,
        )
        await db_session.commit()
        break

    response = await client.get(
        "/api/v1/admin/permissions/registry",
        headers={"Authorization": "admin-registry-token"},
    )

    assert response.status_code == 200
    payload = response.json()
    assert payload["code"] == 200
    assert payload["data"][0]["permission_key"] == "iam:permission:list"
