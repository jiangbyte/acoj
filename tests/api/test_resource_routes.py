from sqlalchemy.ext.asyncio import AsyncSession

from app.deps.db import get_db_session
from app.modules.iam.enums import ResourceModuleClient, ResourceType
from app.modules.iam.resource.model import SysResource, SysResourceModule


async def test_portal_current_resources_are_public(client):
    override = client._transport.app.dependency_overrides[get_db_session]
    async for session in override():
        db_session: AsyncSession = session
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
                    path="/demo",
                    component="/demo/index.vue",
                    sort=2,
                ),
            ]
        )
        await db_session.commit()
        break

    response = await client.get("/api/v1/portal/sys/resources/current")

    assert response.status_code == 200
    assert response.json()["code"] == 200
    data = response.json()["data"]
    assert [item["id"] for item in data] == ["resource_portal"]
    assert data[0]["module_client"] == ResourceModuleClient.PORTAL.value
