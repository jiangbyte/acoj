from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountStatusEnum, AccountType, DataScope
from app.core.security.password import hash_password
from app.core.security.session import SessionPayload, session_store
from app.deps.db import get_db_session
from app.modules.iam.account.model import SysAccount
from app.modules.iam.role.model import SysRole
from app.modules.iam.enums import RoleScopeType


async def _seed_admin(client, token: str, permissions: list[str]) -> SysAccount:
    override = client._transport.app.dependency_overrides[get_db_session]
    async for session in override():
        db_session: AsyncSession = session
        account = SysAccount(
            account=f"{token}_account",
            password_hash=hash_password("Admin@123456"),
            account_type=AccountType.ADMIN.value,
            account_status=AccountStatusEnum.ENABLED.value,
            name=f"{token} Account",
            nickname=f"{token} Account",
        )
        db_session.add(account)
        await db_session.flush()
        await session_store.set(
            SessionPayload(
                token=token,
                account_id=account.id,
                account_type=AccountType.ADMIN.value,
                role_ids=[],
                dept_ids=[],
                group_ids=[],
                permission_keys=permissions,
                permission_grants=[],
            ),
            ttl_seconds=3600,
        )
        await db_session.commit()
        return account
    raise AssertionError("Expected test database session")


async def test_permission_tree_selector_route_lists_resources(client, monkeypatch):
    async def fake_permission_tree_selector(self):
        return ["file:file:page[page]", "iam:role:grantpermission[给角色授权权限]"]

    monkeypatch.setattr(
        "app.modules.iam.role.service.RoleService.permission_tree_selector",
        fake_permission_tree_selector,
    )
    token = "admin-permission-tree-token"
    await _seed_admin(client, token, ["iam:role:permissiontree"])

    response = await client.get(
        "/api/v1/admin/sys/roles/permission-tree-selector",
        headers={"Authorization": token},
    )

    assert response.status_code == 200
    assert response.json()["data"] == [
        "file:file:page[page]",
        "iam:role:grantpermission[给角色授权权限]",
    ]


async def test_role_permission_grant_overwrites_existing_grants(client, monkeypatch):
    async def fake_ensure_registered_permission(permission_key: str) -> None:
        assert permission_key in {"file:file:page", "iam:role:page"}

    monkeypatch.setattr(
        "app.modules.iam.role.service.ensure_registered_permission",
        fake_ensure_registered_permission,
    )
    token = "admin-role-grant-token"
    await _seed_admin(
        client,
        token,
        ["iam:role:grantpermission", "iam:role:ownpermission"],
    )
    override = client._transport.app.dependency_overrides[get_db_session]
    async for session in override():
        db_session: AsyncSession = session
        role = SysRole(
            code="role_permission_grant",
            name="Role Permission Grant",
            category="SYSTEM",
            scope_type=RoleScopeType.PLATFORM.value,
        )
        db_session.add(role)
        await db_session.commit()
        role_id = role.id
        break

    grant_response = await client.post(
        "/api/v1/admin/sys/roles/grant-permission",
        headers={"Authorization": token},
        json={
            "id": role_id,
            "grant_info_list": [
                {
                    "permission_key": "file:file:page",
                    "data_scope": DataScope.ALL.value,
                    "custom_scope_dept_ids": [],
                }
            ],
        },
    )
    assert grant_response.status_code == 200

    own_response = await client.get(
        "/api/v1/admin/sys/roles/own-permission",
        headers={"Authorization": token},
        params={"id": role_id},
    )
    assert own_response.status_code == 200
    assert own_response.json()["data"] == {
        "id": role_id,
        "grant_info_list": [
            {
                "permission_key": "file:file:page",
                "data_scope": DataScope.ALL.value,
                "custom_scope_dept_ids": [],
            }
        ],
    }


async def test_account_permission_grant_overwrites_existing_grants(client, monkeypatch):
    async def fake_ensure_registered_permission(permission_key: str) -> None:
        assert permission_key == "file:file:page"

    monkeypatch.setattr(
        "app.modules.iam.account.service.ensure_registered_permission",
        fake_ensure_registered_permission,
    )
    token = "admin-account-grant-token"
    account = await _seed_admin(
        client,
        token,
        ["iam:account:grantpermission", "iam:account:ownpermission"],
    )

    grant_response = await client.post(
        "/api/v1/admin/sys/accounts/grant-permission",
        headers={"Authorization": token},
        json={
            "id": account.id,
            "grant_info_list": [
                {
                    "permission_key": "file:file:page",
                    "data_scope": DataScope.SELF.value,
                    "custom_scope_dept_ids": [],
                }
            ],
        },
    )
    assert grant_response.status_code == 200

    own_response = await client.get(
        "/api/v1/admin/sys/accounts/own-permission",
        headers={"Authorization": token},
        params={"id": account.id},
    )
    assert own_response.status_code == 200
    assert own_response.json()["data"]["grant_info_list"] == [
        {
            "permission_key": "file:file:page",
            "data_scope": DataScope.SELF.value,
            "custom_scope_dept_ids": [],
        }
    ]
