from app.core.config.enums import AccountType, StatusEnum
from app.core.security.session import SessionPayload
from app.modules.iam.enums import GrantSubjectType, ResourceType
from app.modules.iam.grant.model import SysSubjectResourceGrantRel
from app.modules.iam.resource.model import SysResource
from app.modules.iam.resource.service import ResourceService


def _session(account_id: str, permission_keys: list[str]) -> SessionPayload:
    return SessionPayload(
        token=f"{account_id}-token",
        account_id=account_id,
        account_type=AccountType.ADMIN.value,
        permission_keys=permission_keys,
    )


async def test_current_resources_super_permission_returns_all_enabled_resources(db_session):
    db_session.add_all(
        [
            SysResource(
                id="resource_root",
                code="root",
                name="Root",
                resource_type=ResourceType.CATALOG.value,
                sort=1,
            ),
            SysResource(
                id="resource_child",
                parent_id="resource_root",
                code="child",
                name="Child",
                resource_type=ResourceType.MENU.value,
                sort=2,
            ),
            SysResource(
                id="resource_disabled",
                code="disabled",
                name="Disabled",
                resource_type=ResourceType.MENU.value,
                status=StatusEnum.DISABLED.value,
                sort=3,
            ),
        ]
    )
    await db_session.commit()

    resources = await ResourceService(db_session).list_current_resources(
        _session("admin", ["*:*:*"])
    )

    assert [resource.id for resource in resources] == ["resource_root", "resource_child"]


async def test_current_resources_regular_account_returns_granted_resources_with_parents(db_session):
    db_session.add_all(
        [
            SysResource(
                id="resource_root",
                code="root",
                name="Root",
                resource_type=ResourceType.CATALOG.value,
                sort=1,
            ),
            SysResource(
                id="resource_granted",
                parent_id="resource_root",
                code="granted",
                name="Granted",
                resource_type=ResourceType.MENU.value,
                module="iam",
                path="/iam/granted",
                component="/iam/granted/index.vue",
                sort=2,
            ),
            SysResource(
                id="resource_other",
                parent_id="resource_root",
                code="other",
                name="Other",
                resource_type=ResourceType.MENU.value,
                sort=3,
            ),
            SysSubjectResourceGrantRel(
                subject_type=GrantSubjectType.ACCOUNT.value,
                subject_id="account_1",
                resource_id="resource_granted",
            ),
        ]
    )
    await db_session.commit()

    resources = await ResourceService(db_session).list_current_resources(
        _session("account_1", ["iam:resource:list"])
    )
    tree = await ResourceService(db_session).list_resource_tree(
        _session("account_1", ["iam:resource:list"])
    )

    assert [resource.id for resource in resources] == ["resource_root", "resource_granted"]
    assert [node.id for node in tree] == ["resource_root"]
    assert [node.id for node in tree[0].children] == ["resource_granted"]
    assert tree[0].children[0].parent_id == "resource_root"
    assert tree[0].children[0].module == "iam"
    assert tree[0].children[0].path == "/iam/granted"
    assert tree[0].children[0].component == "/iam/granted/index.vue"
    assert tree[0].children[0].status == StatusEnum.ENABLED.value


async def test_resource_tree_regular_account_without_grants_returns_empty(db_session):
    db_session.add(
        SysResource(
            id="resource_root",
            code="root",
            name="Root",
            resource_type=ResourceType.CATALOG.value,
        )
    )
    await db_session.commit()

    tree = await ResourceService(db_session).list_resource_tree(
        _session("account_without_grant", ["iam:resource:list"])
    )

    assert tree == []
