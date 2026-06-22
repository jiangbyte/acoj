from app.core.config.enums import (
    AccountStatusEnum,
    GrantSubjectType,
    ResourceType,
    RoleScopeType,
    UserType,
)
from app.modules.iam.model import SysAccount
from app.modules.iam.schema import (
    AccountRoleAssignRequest,
    GroupCreateRequest,
    GroupRoleAssignRequest,
    ResourceCreateRequest,
    ResourcePermissionBindRequest,
    RoleCreateRequest,
    SubjectResourceGrantRequest,
)
from app.modules.iam.service import IAMService


async def test_assign_account_role_success(db_session):
    role = await IAMService(db_session).create_role(
        RoleCreateRequest(
            code="r1",
            name="Role1",
            category="SYSTEM",
            scope_type=RoleScopeType.PLATFORM.value,
        )
    )
    account = SysAccount(
        account="a1",
        password_hash="x",
        account_type=UserType.ADMIN.value,
        account_status=AccountStatusEnum.ENABLED.value,
        name="A1",
        nickname="A1",
    )
    db_session.add(account)
    await db_session.flush()
    await db_session.commit()
    relation = await IAMService(db_session).assign_account_role(
        AccountRoleAssignRequest(account_id=account.id, role_id=role.id)
    )
    await db_session.commit()
    assert relation.account_id == account.id
    assert relation.role_id == role.id


async def test_bind_resource_permission_success(db_session, monkeypatch):
    async def fake_get_permission_definition(permission_key: str):
        return {
            "permission_key": permission_key,
            "module": "iam",
            "source": "tests.unit.test_iam_relations",
            "methods": ["POST"],
            "login_scopes": ["admin"],
            "routes": [],
        }

    monkeypatch.setattr(
        "app.modules.iam.service.get_permission_definition",
        fake_get_permission_definition,
    )

    resource = await IAMService(db_session).create_resource(
        ResourceCreateRequest(
            code="iam:button:create",
            name="Create Button",
            resource_type=ResourceType.BUTTON.value,
            module="iam",
        )
    )
    relation = await IAMService(db_session).bind_resource_permission(
        ResourcePermissionBindRequest(resource_id=resource.id, permission_key="iam:account:create")
    )
    await db_session.commit()
    assert relation.resource_id == resource.id
    assert relation.permission_key == "iam:account:create"


async def test_assign_group_role_success(db_session):
    group = await IAMService(db_session).create_group(
        GroupCreateRequest(name="Group1", description="Test group")
    )
    role = await IAMService(db_session).create_role(
        RoleCreateRequest(
            code="r3",
            name="Role3",
            category="SYSTEM",
            scope_type=RoleScopeType.PLATFORM.value,
        )
    )
    relation = await IAMService(db_session).assign_group_role(
        GroupRoleAssignRequest(group_id=group.id, role_id=role.id)
    )
    await db_session.commit()
    assert relation.group_id == group.id
    assert relation.role_id == role.id


async def test_grant_subject_resource_success(db_session):
    role = await IAMService(db_session).create_role(
        RoleCreateRequest(
            code="r4",
            name="Role4",
            category="SYSTEM",
            scope_type=RoleScopeType.PLATFORM.value,
        )
    )
    resource = await IAMService(db_session).create_resource(
        ResourceCreateRequest(
            code="iam:resource:grant",
            name="Grant Resource",
            resource_type=ResourceType.BUTTON.value,
            module="iam",
        )
    )
    relation = await IAMService(db_session).grant_subject_resource(
        SubjectResourceGrantRequest(
            subject_type=GrantSubjectType.ROLE.value,
            subject_id=role.id,
            resource_id=resource.id,
        )
    )
    await db_session.commit()
    assert relation.subject_id == role.id
    assert relation.resource_id == resource.id
