from collections.abc import Mapping, Sequence

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import ResourceType
from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.iam.permission.service import ensure_registered_permission
from app.modules.iam.resource.repository import ResourceRepository, ResourceTreeRecord
from app.modules.iam.resource.schema import (
    ResourceAdminPageQuery,
    ResourceCreateRequest,
    ResourcePermissionBindRequest,
    ResourceTreeNode,
    ResourceUpdateRequest,
    SysResourcePermissionRelSchema,
    SysResourceSchema,
)
from app.platform.db.transaction import transactional


class ResourceService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = ResourceRepository(db)

    async def create(self, payload: ResourceCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: ResourceUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> SysResourceSchema:
        return to_schema(SysResourceSchema, await self.repo.get_required(query.id))

    async def page_admin(self, query: ResourceAdminPageQuery) -> PageData[SysResourceSchema]:
        items, total = await self.repo.page_admin(query)
        return build_page(query.pagination, total, to_schema_list(SysResourceSchema, items))

    async def bind_resource_permission(
        self,
        payload: ResourcePermissionBindRequest,
    ) -> SysResourcePermissionRelSchema:
        await ensure_registered_permission(payload.permission_key)
        async with transactional(self.db):
            return to_schema(
                SysResourcePermissionRelSchema,
                await self.repo.bind_resource_permission(payload),
            )

    async def list_resource_tree(self) -> list[ResourceTreeNode]:
        return _build_resource_tree_nodes(await self.repo.get_resource_tree())


def _build_resource_tree_nodes(
    items: Sequence[ResourceTreeRecord | ResourceTreeNode | Mapping[str, object]],
) -> list[ResourceTreeNode]:
    nodes: list[ResourceTreeNode] = []
    for item in items:
        raw_item: Mapping[str, object] = (
            item.model_dump() if isinstance(item, ResourceTreeNode) else item
        )
        nodes.append(
            ResourceTreeNode(
                id=str(raw_item["id"]),
                code=str(raw_item["code"]),
                name=str(raw_item["name"]),
                resource_type=ResourceType(str(raw_item["resource_type"])),
                children=_build_resource_tree_nodes(raw_item.get("children", [])),  # type: ignore[arg-type]
            )
        )
    return nodes
