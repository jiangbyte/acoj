from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.problem.objective_answer.repository import OjObjectiveAnswerRepository
from app.modules.oj.problem.objective_answer.schema import (
    OjObjectiveAnswerAdminPageQuery,
    OjObjectiveAnswerCreateRequest,
    OjObjectiveAnswerSchema,
    OjObjectiveAnswerUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjObjectiveAnswerService:
    """OJ objective answer 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjObjectiveAnswerRepository(db)

    async def create(self, payload: OjObjectiveAnswerCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjObjectiveAnswerUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjObjectiveAnswerSchema:
        return to_schema(OjObjectiveAnswerSchema, await self.repo.get_required(query.id))

    async def page_admin(
        self, query: OjObjectiveAnswerAdminPageQuery
    ) -> PageData[OjObjectiveAnswerSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjObjectiveAnswerSchema, items))
