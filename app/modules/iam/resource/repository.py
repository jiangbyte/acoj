from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import StatusEnum
from app.core.exceptions.business import NotFoundError
from app.modules.iam.enums import ResourceType
from app.modules.iam.reference_guard import (
    count_resource_references,
    ensure_not_self_or_descendant,
    ensure_parent_exists,
    raise_if_referenced,
)
from app.modules.iam.resource.model import SysResource, SysResourcePermissionRel
from app.modules.iam.resource.schema import (
    PermissionRegistryItem,
    ResourceAdminPageQuery,
    ResourceCreateRequest,
    ResourceGrantMenuOption,
    ResourceGrantModuleOption,
    ResourcePermissionOption,
    ResourcePermissionBindRequest,
    ResourceUpdateRequest,
)


class ResourceRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: ResourceCreateRequest) -> None:
        await ensure_parent_exists(self.db, SysResource, payload.parent_id, "Resource")
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
        await ensure_parent_exists(self.db, SysResource, payload.parent_id, "Resource")
        await ensure_not_self_or_descendant(
            self.db,
            SysResource,
            payload.id,
            payload.parent_id,
            "Resource",
        )
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, resource_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(resource_ids))
        if not unique_ids:
            return
        stmt = select(SysResource.id).where(SysResource.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("Resource not found")
        raise_if_referenced("Resource", await count_resource_references(self.db, unique_ids))
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
        stmt = (
            select(SysResource)
            .where(SysResource.status == StatusEnum.ENABLED.value)
            .order_by(
                SysResource.sort.asc(),
                SysResource.id.asc(),
            )
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
        stmt = (
            select(SysResourcePermissionRel)
            .where(SysResourcePermissionRel.status == StatusEnum.ENABLED.value)
            .order_by(SysResourcePermissionRel.sort.asc(), SysResourcePermissionRel.id.asc())
        )
        return list((await self.db.execute(stmt)).scalars().all())

    async def list_resources_by_ids_with_parents(self, resource_ids: list[str]) -> list[SysResource]:
        unique_ids = set(resource_ids)
        if not unique_ids:
            return []
        all_resources = await self.list_resources()
        resource_map = {resource.id: resource for resource in all_resources}
        result_ids: set[str] = set()
        for resource_id in unique_ids:
            current = resource_map.get(resource_id)
            while current:
                result_ids.add(current.id)
                current = resource_map.get(current.parent_id or "")
        return [resource for resource in all_resources if resource.id in result_ids]

    async def list_all_resource_grant_modules(self) -> list[ResourceGrantModuleOption]:
        resources = await self.list_resources()
        permissions = await self.list_resource_permissions()
        permission_map: dict[str, list[ResourcePermissionOption]] = {}
        for permission in permissions:
            permission_map.setdefault(permission.resource_id, []).append(
                ResourcePermissionOption(
                    id=permission.id,
                    permission_key=permission.permission_key,
                    title=permission.description or permission.permission_key,
                    data_scope=permission.data_scope,
                )
            )
        resource_map = {resource.id: resource for resource in resources}
        child_permission_map: dict[str, list[ResourcePermissionOption]] = {}
        for resource in resources:
            if resource.resource_type not in {ResourceType.BUTTON.value, ResourceType.ACTION.value}:
                continue
            if not resource.parent_id:
                continue
            options = permission_map.get(resource.id)
            if not options:
                options = [
                    ResourcePermissionOption(
                        id=resource.id,
                        permission_key=resource.code,
                        title=resource.name,
                    )
                ]
            child_permission_map.setdefault(resource.parent_id, []).extend(options)
        module_map: dict[str, ResourceGrantModuleOption] = {}
        for resource in resources:
            if resource.resource_type in {ResourceType.BUTTON.value, ResourceType.ACTION.value}:
                continue
            module_id = resource.module or "default"
            module = module_map.setdefault(
                module_id,
                ResourceGrantModuleOption(id=module_id, title=module_id, menu=[]),
            )
            parent = resource_map.get(resource.parent_id or "")
            module.menu.append(
                ResourceGrantMenuOption(
                    id=resource.id,
                    module=module_id,
                    parent_id=resource.parent_id,
                    parent_name=parent.name if parent else "ROOT",
                    title=resource.name,
                    button=permission_map.get(resource.id, []) + child_permission_map.get(resource.id, []),
                )
            )
        return sorted(module_map.values(), key=lambda item: item.id)
