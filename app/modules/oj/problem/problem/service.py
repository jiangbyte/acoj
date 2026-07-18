from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.problem.problem.repository import OjProblemRepository
from app.modules.oj.problem.problem.schema import (
    OjProblemAdminPageQuery,
    OjProblemCreateRequest,
    OjProblemSchema,
    OjProblemUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjProblemService:
    """OJ problem 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjProblemRepository(db)

    async def create(self, payload: OjProblemCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjProblemUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjProblemSchema:
        return to_schema(OjProblemSchema, await self.repo.get_required(query.id))

    async def page_admin(self, query: OjProblemAdminPageQuery) -> PageData[OjProblemSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjProblemSchema, items))
