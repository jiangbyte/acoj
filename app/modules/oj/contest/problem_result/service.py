from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.contest.problem_result.repository import OjContestProblemResultRepository
from app.modules.oj.contest.problem_result.schema import (
    OjContestProblemResultAdminPageQuery,
    OjContestProblemResultCreateRequest,
    OjContestProblemResultSchema,
    OjContestProblemResultUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjContestProblemResultService:
    """OJ contest problem result 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjContestProblemResultRepository(db)

    async def create(self, payload: OjContestProblemResultCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjContestProblemResultUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjContestProblemResultSchema:
        return to_schema(OjContestProblemResultSchema, await self.repo.get_required(query.id))

    async def page_admin(
        self, query: OjContestProblemResultAdminPageQuery
    ) -> PageData[OjContestProblemResultSchema]:
        items, total = await self.repo.page(query)
        return build_page(
            query.pagination, total, to_schema_list(OjContestProblemResultSchema, items)
        )
