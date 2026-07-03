from app.core.config.enums import AccountType, StatusEnum
from app.core.response.pagination import PageQuery
from app.core.security.session import SessionPayload
from app.modules.iam.enums import GrantSubjectType, ResourceModuleClient, ResourceType
from app.modules.iam.grant.model import SysSubjectResourceGrantRel
from app.modules.iam.resource.model import SysResource, SysResourceModule
from app.modules.iam.resource.schema import ResourceModuleAdminPageQuery
from app.modules.iam.resource.service import ResourceModuleService, ResourceService


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
            SysResourceModule(
                id="module_iam",
                code="iam",
                name="IAM",
            ),
            SysResource(
                id="resource_root",
                code="root",
                name="Root",
                resource_type=ResourceType.CATALOG.value,
                module_id="module_iam",
                sort=1,
            ),
            SysResource(
                id="resource_granted",
                parent_id="resource_root",
                code="granted",
                name="Granted",
                resource_type=ResourceType.MENU.value,
                module_id="module_iam",
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
    assert tree[0].children[0].module_id == "module_iam"
    assert tree[0].children[0].module_id_name == "IAM"
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


async def test_current_resources_filters_by_portal_module_client(db_session):
    db_session.add_all(
        [
            SysResourceModule(
                id="module_admin",
                code="admin",
                name="Admin",
                client=ResourceModuleClient.ADMIN.value,
            ),
            SysResourceModule(
                id="module_portal",
                code="portal",
                name="Portal",
                client=ResourceModuleClient.PORTAL.value,
            ),
            SysResource(
                id="resource_admin",
                code="admin-resource",
                name="Admin Resource",
                resource_type=ResourceType.MENU.value,
                module_id="module_admin",
                sort=1,
            ),
            SysResource(
                id="resource_portal",
                code="portal-resource",
                name="Portal Resource",
                resource_type=ResourceType.MENU.value,
                module_id="module_portal",
                sort=2,
            ),
        ]
    )
    await db_session.commit()

    resources = await ResourceService(db_session).list_current_resources(
        _session("portal", ["*:*:*"]),
        module_client=ResourceModuleClient.PORTAL,
    )

    assert [resource.id for resource in resources] == ["resource_portal"]
    assert resources[0].module_client == ResourceModuleClient.PORTAL.value


async def test_public_portal_resources_return_enabled_portal_resources_without_session(db_session):
    db_session.add_all(
        [
            SysResourceModule(
                id="module_admin",
                code="admin",
                name="Admin",
                client=ResourceModuleClient.ADMIN.value,
            ),
            SysResourceModule(
                id="module_portal",
                code="portal",
                name="Portal",
                client=ResourceModuleClient.PORTAL.value,
            ),
            SysResource(
                id="resource_admin",
                code="admin-resource",
                name="Admin Resource",
                resource_type=ResourceType.MENU.value,
                module_id="module_admin",
                sort=1,
            ),
            SysResource(
                id="resource_portal",
                code="portal-resource",
                name="Portal Resource",
                resource_type=ResourceType.MENU.value,
                module_id="module_portal",
                sort=2,
            ),
            SysResource(
                id="resource_portal_disabled",
                code="portal-disabled-resource",
                name="Portal Disabled Resource",
                resource_type=ResourceType.MENU.value,
                module_id="module_portal",
                status=StatusEnum.DISABLED.value,
                sort=3,
            ),
        ]
    )
    await db_session.commit()

    resources = await ResourceService(db_session).list_public_portal_resources()

    assert [resource.id for resource in resources] == ["resource_portal"]
    assert resources[0].module_client == ResourceModuleClient.PORTAL.value


async def test_resource_tree_filters_by_module_id_and_client(db_session):
    db_session.add_all(
        [
            SysResourceModule(
                id="module_admin_a",
                code="admin-a",
                name="Admin A",
                client=ResourceModuleClient.ADMIN.value,
                sort=1,
            ),
            SysResourceModule(
                id="module_admin_b",
                code="admin-b",
                name="Admin B",
                client=ResourceModuleClient.ADMIN.value,
                sort=2,
            ),
            SysResource(
                id="resource_admin_a",
                code="admin-a-root",
                name="Admin A Root",
                resource_type=ResourceType.CATALOG.value,
                module_id="module_admin_a",
                sort=1,
            ),
            SysResource(
                id="resource_admin_b",
                code="admin-b-root",
                name="Admin B Root",
                resource_type=ResourceType.CATALOG.value,
                module_id="module_admin_b",
                sort=2,
            ),
        ]
    )
    await db_session.commit()

    tree = await ResourceService(db_session).list_resource_tree(
        _session("admin", ["*:*:*"]),
        module_id="module_admin_b",
        module_client=ResourceModuleClient.ADMIN,
    )

    assert [node.id for node in tree] == ["resource_admin_b"]
    assert tree[0].module_id_name == "Admin B"
    assert tree[0].module_client == ResourceModuleClient.ADMIN.value


async def test_resource_module_selector_and_page_filter_by_client(db_session):
    db_session.add_all(
        [
            SysResourceModule(
                id="module_admin",
                code="admin",
                name="Admin",
                client=ResourceModuleClient.ADMIN.value,
                sort=1,
            ),
            SysResourceModule(
                id="module_portal",
                code="portal",
                name="Portal",
                client=ResourceModuleClient.PORTAL.value,
                sort=2,
            ),
        ]
    )
    await db_session.commit()

    service = ResourceModuleService(db_session)
    selector = await service.selector(ResourceModuleClient.PORTAL)
    page = await service.page_admin(
        ResourceModuleAdminPageQuery(
            pagination=PageQuery(current=1, size=20),
            client=ResourceModuleClient.PORTAL,
        )
    )

    assert [item.id for item in selector] == ["module_portal"]
    assert [item.id for item in page.records] == ["module_portal"]
