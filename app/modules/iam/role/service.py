from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.iam.role.repository import RoleRepository
from app.modules.iam.role.schema import (
    RoleAdminPageQuery,
    RoleCreateRequest,
    RoleUpdateRequest,
    SysRoleSchema,
)
from app.platform.db.transaction import transactional


class RoleService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = RoleRepository(db)

    async def create(self, payload: RoleCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: RoleUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> SysRoleSchema:
        return to_schema(SysRoleSchema, await self.repo.get_required(query.id))

    async def page_admin(self, query: RoleAdminPageQuery) -> PageData[SysRoleSchema]:
        items, total = await self.repo.page_admin(query)
        return build_page(query.pagination, total, to_schema_list(SysRoleSchema, items))
