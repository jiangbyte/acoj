from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.community.solution.repository import OjSolutionRepository
from app.modules.oj.community.solution.schema import (
    OjSolutionAdminPageQuery,
    OjSolutionCreateRequest,
    OjSolutionSchema,
    OjSolutionUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjSolutionService:
    """OJ solution 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjSolutionRepository(db)

    async def create(self, payload: OjSolutionCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjSolutionUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjSolutionSchema:
        return to_schema(OjSolutionSchema, await self.repo.get_required(query.id))

    async def page_admin(self, query: OjSolutionAdminPageQuery) -> PageData[OjSolutionSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjSolutionSchema, items))
