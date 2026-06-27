from typing import TypedDict

from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import ResourceType, StatusEnum
from app.core.exceptions.business import NotFoundError
from app.modules.iam.resource.model import SysResource, SysResourcePermissionRel
from app.modules.iam.resource.schema import (
    ResourceAdminPageQuery,
    ResourceCreateRequest,
    ResourcePermissionBindRequest,
    ResourceUpdateRequest,
)


class ResourceTreeRecord(TypedDict):
    id: str
    code: str
    name: str
    resource_type: ResourceType | str
    children: list["ResourceTreeRecord"]


class ResourceRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: ResourceCreateRequest) -> None:
        resource = SysResource(**payload.model_dump())
        self.db.add(resource)
        await self.db.flush()

    async def get_by_id(self, resource_id: str) -> SysResource | None:
        return await self.db.get(SysResource, resource_id)

    async def get_required(self, resource_id: str) -> SysResource:
        entity = await self.get_by_id(resource_id)
        if entity is None:
            raise NotFoundError("Resource not found")
        return entity

    async def update(self, payload: ResourceUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, resource_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(resource_ids))
        stmt = select(SysResource.id).where(SysResource.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("Resource not found")
        await self.db.execute(delete(SysResource).where(SysResource.id.in_(unique_ids)))

    async def page_admin(self, query: ResourceAdminPageQuery) -> tuple[list[SysResource], int]:
        stmt: Select[tuple[SysResource]] = select(SysResource)
        count_stmt = select(func.count(SysResource.id))
        filters = []
        if query.code:
            filters.append(SysResource.code.contains(query.code))
        if query.name:
            filters.append(SysResource.name.contains(query.name))
        if query.resource_type:
            filters.append(SysResource.resource_type == query.resource_type.value)
        if query.module:
            filters.append(SysResource.module == query.module)
        if query.parent_id:
            filters.append(SysResource.parent_id == query.parent_id)
        if query.status:
            filters.append(SysResource.status == query.status)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(SysResource.sort.asc(), SysResource.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        resources = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return resources, total

    async def list_resources(self) -> list[SysResource]:
        stmt = select(SysResource).where(SysResource.status == StatusEnum.ENABLED.value).order_by(
            SysResource.sort.asc(),
            SysResource.id.asc(),
        )
        return list((await self.db.execute(stmt)).scalars().all())

    async def bind_resource_permission(
        self,
        payload: ResourcePermissionBindRequest,
    ) -> SysResourcePermissionRel:
        if not await self.db.get(SysResource, payload.resource_id):
            raise NotFoundError("Resource not found")
        relation = SysResourcePermissionRel(**payload.model_dump())
        self.db.add(relation)
        await self.db.flush()
        return relation

    async def list_resource_permissions(self) -> list[SysResourcePermissionRel]:
        stmt = select(SysResourcePermissionRel).where(
            SysResourcePermissionRel.status == StatusEnum.ENABLED.value
        ).order_by(SysResourcePermissionRel.sort.asc(), SysResourcePermissionRel.id.asc())
        return list((await self.db.execute(stmt)).scalars().all())

    async def get_resource_tree(self) -> list[ResourceTreeRecord]:
        resources = await self.list_resources()
        node_map: dict[str, ResourceTreeRecord] = {
            resource.id: {
                "id": resource.id,
                "code": resource.code,
                "name": resource.name,
                "resource_type": resource.resource_type,
                "children": [],
            }
            for resource in resources
        }
        roots: list[ResourceTreeRecord] = []
        for resource in resources:
            if resource.parent_id and resource.parent_id in node_map:
                node_map[resource.parent_id]["children"].append(node_map[resource.id])
            else:
                roots.append(node_map[resource.id])
        return roots
