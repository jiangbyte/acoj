import pytest

from app.core.config.constants import SUPER_ADMIN_ROLE_CODE
from app.core.config.enums import (
    AccountStatusEnum,
    AccountType,
    DataScope,
)
from app.core.exceptions.business import AuthenticationError
from app.core.security.password import hash_password
from app.modules.auth.schema import LoginPayload
from app.modules.auth.service import AuthService
from app.modules.iam.enums import (
    AccountIdentityType,
    GrantEffect,
    GrantSubjectType,
    ResourceType,
    RoleScopeType,
)
from app.modules.iam.account.model import (
    SysAccount,
    SysAccountGroupRel,
    SysAccountIdentity,
    SysAccountRoleRel,
)
from app.modules.iam.grant.model import SysSubjectPermissionGrantRel, SysSubjectResourceGrantRel
from app.modules.iam.grant.repository import GrantRepository
from app.modules.iam.group.model import SysGroup, SysGroupRoleRel
from app.modules.iam.resource.model import SysResource, SysResourcePermissionRel
from app.modules.iam.role.model import SysRole


async def _seed_account(
    db_session,
    *,
    identifier: str,
    password: str,
    account_type: AccountType,
) -> SysAccount:
    account = SysAccount(
        password_hash=hash_password(password),
        account_type=account_type.value,
        account_status=AccountStatusEnum.ENABLED.value,
    )
    db_session.add(account)
    await db_session.flush()
    db_session.add(
        SysAccountIdentity(
            account_id=account.id,
            identity_type=AccountIdentityType.ACCOUNT.value,
            identifier=identifier,
            verified=True,
            is_primary=True,
        )
    )
    return account


async def test_admin_login_success(db_session):
    account = await _seed_account(
        db_session,
        identifier="admin",
        password="Admin@123456",
        account_type=AccountType.ADMIN,
    )

    role = SysRole(
        code=SUPER_ADMIN_ROLE_CODE,
        name="Super Admin",
        category="SYSTEM",
        scope_type=RoleScopeType.PLATFORM.value,
    )
    resource = SysResource(
        code="iam:user:list",
        name="Account List Resource",
        resource_type=ResourceType.BUTTON.value,
    )
    db_session.add_all([role, resource])
    await db_session.flush()
    db_session.add(SysResourcePermissionRel(resource_id=resource.id, permission_key="iam:account:list"))
    db_session.add(
        SysSubjectResourceGrantRel(
            subject_type=GrantSubjectType.ROLE.value,
            subject_id=role.id,
            resource_id=resource.id,
        )
    )
    db_session.add(SysAccountRoleRel(account_id=account.id, role_id=role.id))
    await db_session.commit()

    payload = await AuthService(db_session).login(
        LoginPayload(account="admin", password="Admin@123456", account_type=AccountType.ADMIN)
    )
    assert payload.account_id == account.id
    assert payload.account_type == AccountType.ADMIN.value
    assert "iam:account:list" in payload.permission_keys
    assert "*:*:*" in payload.permission_keys


async def test_portal_account_cannot_login_admin_account_type(db_session):
    await _seed_account(
        db_session,
        identifier="portal_account",
        password="Portal@123456",
        account_type=AccountType.PORTAL,
    )
    await db_session.commit()

    with pytest.raises(AuthenticationError):
        await AuthService(db_session).login(
            LoginPayload(
                account="portal_account",
                password="Portal@123456",
                account_type=AccountType.ADMIN,
            )
        )


async def test_permission_grant_priority_group_over_role_and_account_deny(db_session):
    account = SysAccount(
        password_hash=hash_password("Admin@123456"),
        account_type=AccountType.ADMIN.value,
        account_status=AccountStatusEnum.ENABLED.value,
    )
    role = SysRole(
        code="priority_role",
        name="Priority Role",
        category="SYSTEM",
        scope_type=RoleScopeType.PLATFORM.value,
    )
    group = SysGroup(name="Priority Group")
    db_session.add_all([account, role, group])
    await db_session.flush()
    db_session.add_all(
        [
            SysAccountRoleRel(account_id=account.id, role_id=role.id),
            SysAccountGroupRel(account_id=account.id, group_id=group.id),
            SysGroupRoleRel(group_id=group.id, role_id=role.id),
            SysSubjectPermissionGrantRel(
                subject_type=GrantSubjectType.ROLE.value,
                subject_id=role.id,
                permission_key="sys:file:page",
                data_scope=DataScope.DEPT.value,
                custom_scope_dept_ids=[],
            ),
            SysSubjectPermissionGrantRel(
                subject_type=GrantSubjectType.GROUP.value,
                subject_id=group.id,
                permission_key="sys:file:page",
                data_scope=DataScope.CUSTOM.value,
                custom_scope_dept_ids=["dept_2"],
            ),
        ]
    )
    await db_session.commit()

    authorization = await GrantRepository(db_session).get_account_authorization(account.id)
    assert authorization["permission_grants"] == [
        {
            "permission_key": "sys:file:page",
            "data_scope": DataScope.CUSTOM.value,
            "custom_scope_dept_ids": ["dept_2"],
            "effect": GrantEffect.ALLOW.value,
            "source_type": GrantSubjectType.GROUP.value,
            "source_id": group.id,
        }
    ]
    assert authorization["permission_keys"] == ["sys:file:page"]

    db_session.add(
        SysSubjectPermissionGrantRel(
            subject_type=GrantSubjectType.ACCOUNT.value,
            subject_id=account.id,
            permission_key="sys:file:page",
            data_scope=DataScope.ALL.value,
            custom_scope_dept_ids=[],
            effect=GrantEffect.DENY.value,
        )
    )
    await db_session.commit()

    authorization = await GrantRepository(db_session).get_account_authorization(account.id)
    assert authorization["permission_grants"] == []
    assert authorization["permission_keys"] == []
