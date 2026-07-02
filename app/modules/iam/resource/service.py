from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import AuthorizationError
from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.core.security.data_scope import resolve_data_scope_dept_ids
from app.core.security.permission_registry import list_permission_resources
from app.core.security.session import SessionPayload
from app.modules.iam.grant.repository import GrantRepository
from app.modules.iam.permission.service import ensure_registered_permission
from app.modules.iam.resource.model import SysResource
from app.modules.iam.resource.repository import ResourceModuleRepository, ResourceRepository
from app.modules.iam.resource.schema import (
    ResourceAdminPageQuery,
    ResourceCreateRequest,
    ResourceModuleAdminPageQuery,
    ResourceModuleCreateRequest,
    ResourceModuleSelectorOption,
    ResourceModuleUpdateRequest,
    ResourcePermissionBindRequest,
    ResourceTreeNode,
    ResourceUpdateRequest,
    SysResourceModuleSchema,
    SysResourcePermissionRelSchema,
    SysResourceSchema,
)
from app.modules.iam.schema import PermissionRegistryItem, ResourceGrantModuleOption
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
        return (await self._build_resource_schemas([await self.repo.get_required(query.id)]))[0]

    async def page_admin(self, query: ResourceAdminPageQuery) -> PageData[SysResourceSchema]:
        items, total = await self.repo.page_admin(query)
        return build_page(query.pagination, total, await self._build_resource_schemas(items))

    async def bind_resource_permission(
        self,
        payload: ResourcePermissionBindRequest,
        session: SessionPayload | None = None,
    ) -> SysResourcePermissionRelSchema:
        if session is not None:
            await self._ensure_depts_visible(
                session,
                "iam:resource:grant",
                payload.custom_scope_dept_ids,
            )
        await ensure_registered_permission(payload.permission_key)
        async with transactional(self.db):
            return to_schema(
                SysResourcePermissionRelSchema,
                await self.repo.bind_resource_permission(payload),
            )

    async def list_resource_tree(self, session: SessionPayload) -> list[ResourceTreeNode]:
        resources = await self._list_visible_resources(session)
        return await self._build_resource_tree_nodes(resources)

    async def list_current_resources(self, session: SessionPayload) -> list[SysResourceSchema]:
        resources = await self._list_visible_resources(session)
        return await self._build_resource_schemas(resources)

    async def _list_visible_resources(self, session: SessionPayload) -> list[SysResource]:
        if "*:*:*" in session.permission_keys:
            return await self.repo.list_resources()
        resource_ids = await GrantRepository(self.db).get_account_resource_ids(session.account_id)
        return await self.repo.list_resources_by_ids_with_parents(resource_ids)

    async def list_grant_modules(self) -> list[ResourceGrantModuleOption]:
        return await self.repo.list_all_resource_grant_modules()

    async def _build_resource_schemas(
        self,
        resources: list[SysResource],
    ) -> list[SysResourceSchema]:
        module_name_map = await self.repo.list_module_name_map(
            [resource.module_id for resource in resources if resource.module_id]
        )
        schemas = to_schema_list(SysResourceSchema, resources)
        for schema in schemas:
            schema.module_id_name = module_name_map.get(schema.module_id or "")
        return schemas

    async def _build_resource_tree_nodes(
        self,
        resources: list[SysResource],
    ) -> list[ResourceTreeNode]:
        module_name_map = await self.repo.list_module_name_map(
            [resource.module_id for resource in resources if resource.module_id]
        )
        return _build_resource_tree_nodes(resources, module_name_map)

    async def list_permission_registry_items(self) -> list[PermissionRegistryItem]:
        resources = await list_permission_resources()
        items: list[PermissionRegistryItem] = []
        for resource in resources:
            index = resource.find("[")
            permission_key = resource[:index] if index > -1 else resource
            name = (
                resource[index + 1 : -1]
                if index > -1 and resource.endswith("]")
                else permission_key
            )
            items.append(PermissionRegistryItem(permission_key=permission_key, name=name))
        return sorted(items, key=lambda item: item.permission_key)

    async def _ensure_depts_visible(
        self,
        session: SessionPayload,
        permission_key: str,
        dept_ids: list[str],
    ) -> None:
        unique_ids = list(dict.fromkeys(dept_ids))
        if not unique_ids:
            return
        visible_dept_ids = await resolve_data_scope_dept_ids(self.db, session, permission_key)
        if visible_dept_ids is None:
            return
        allowed_ids = set(visible_dept_ids)
        if any(dept_id not in allowed_ids for dept_id in unique_ids):
            raise AuthorizationError("Dept is outside current data scope")


class ResourceModuleService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = ResourceModuleRepository(db)

    async def create(self, payload: ResourceModuleCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: ResourceModuleUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> SysResourceModuleSchema:
        return to_schema(SysResourceModuleSchema, await self.repo.get_required(query.id))

    async def page_admin(
        self,
        query: ResourceModuleAdminPageQuery,
    ) -> PageData[SysResourceModuleSchema]:
        items, total = await self.repo.page_admin(query)
        return build_page(query.pagination, total, to_schema_list(SysResourceModuleSchema, items))

    async def selector(self) -> list[ResourceModuleSelectorOption]:
        return to_schema_list(ResourceModuleSelectorOption, await self.repo.list_enabled_modules())


def _build_resource_tree_nodes(
    resources: list[SysResource],
    module_name_map: dict[str, str],
) -> list[ResourceTreeNode]:
    node_map = {
        resource.id: to_schema(ResourceTreeNode, resource)
        for resource in resources
    }
    for node in node_map.values():
        node.module_id_name = module_name_map.get(node.module_id or "")
    roots: list[ResourceTreeNode] = []
    for resource in resources:
        node = node_map[resource.id]
        if resource.parent_id and resource.parent_id in node_map:
            node_map[resource.parent_id].children.append(node)
        else:
            roots.append(node)
    return roots
