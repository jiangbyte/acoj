from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.iam.group.repository import GroupRepository
from app.modules.iam.group.schema import (
    GroupAdminPageQuery,
    GroupCreateRequest,
    GroupRoleAssignRequest,
    GroupUpdateRequest,
    SysGroupRoleRelSchema,
    SysGroupSchema,
)
from app.platform.db.transaction import transactional


class GroupService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = GroupRepository(db)

    async def create(self, payload: GroupCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: GroupUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> SysGroupSchema:
        return to_schema(SysGroupSchema, await self.repo.get_required(query.id))

    async def page_admin(self, query: GroupAdminPageQuery) -> PageData[SysGroupSchema]:
        items, total = await self.repo.page_admin(query)
        return build_page(query.pagination, total, to_schema_list(SysGroupSchema, items))

    async def assign_group_role(self, payload: GroupRoleAssignRequest) -> SysGroupRoleRelSchema:
        async with transactional(self.db):
            return to_schema(SysGroupRoleRelSchema, await self.repo.assign_group_to_role(payload))
