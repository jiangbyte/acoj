import pytest

from app.core.config.enums import (
    AccountStatusEnum,
    AccountType,
)
from app.core.exceptions.business import AuthenticationError
from app.core.security.password import hash_password
from app.modules.auth.schema import LoginPayload
from app.modules.auth.service import AuthService
from app.modules.iam.enums import GrantSubjectType, ResourceType, RoleScopeType
from app.modules.iam.account.model import SysAccount, SysAccountRoleRel
from app.modules.iam.grant.model import SysSubjectResourceGrantRel
from app.modules.iam.resource.model import SysResource, SysResourcePermissionRel
from app.modules.iam.role.model import SysRole


async def test_admin_login_success(db_session):
    account = SysAccount(
        account="admin",
        password_hash=hash_password("Admin@123456"),
        account_type=AccountType.ADMIN.value,
        account_status=AccountStatusEnum.ENABLED.value,
        name="Admin",
        nickname="Admin",
    )
    db_session.add(account)
    await db_session.flush()

    role = SysRole(
        code="super_admin",
        name="Super Admin",
        category="SYSTEM",
        scope_type=RoleScopeType.PLATFORM.value,
    )
    resource = SysResource(
        code="iam:user:list",
        name="Account List Resource",
        resource_type=ResourceType.BUTTON.value,
        module="iam",
    )
    db_session.add_all([role, resource])
    await db_session.flush()
    db_session.add(
        SysResourcePermissionRel(resource_id=resource.id, permission_key="iam:account:list")
    )
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


async def test_portal_account_cannot_login_admin_account_type(db_session):
    account = SysAccount(
        account="portal_account",
        password_hash=hash_password("Portal@123456"),
        account_type=AccountType.PORTAL.value,
        account_status=AccountStatusEnum.ENABLED.value,
        name="Portal Account",
        nickname="Portal Account",
    )
    db_session.add(account)
    await db_session.commit()

    with pytest.raises(AuthenticationError):
        await AuthService(db_session).login(
            LoginPayload(
                account="portal_account",
                password="Portal@123456",
                account_type=AccountType.ADMIN,
            )
        )
