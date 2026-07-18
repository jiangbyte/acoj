from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.problem.sample.repository import OjProblemSampleRepository
from app.modules.oj.problem.sample.schema import (
    OjProblemSampleAdminPageQuery,
    OjProblemSampleCreateRequest,
    OjProblemSampleSchema,
    OjProblemSampleUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjProblemSampleService:
    """OJ problem sample 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjProblemSampleRepository(db)

    async def create(self, payload: OjProblemSampleCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjProblemSampleUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjProblemSampleSchema:
        return to_schema(OjProblemSampleSchema, await self.repo.get_required(query.id))

    async def page_admin(
        self, query: OjProblemSampleAdminPageQuery
    ) -> PageData[OjProblemSampleSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjProblemSampleSchema, items))
