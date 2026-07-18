from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.problem.test_case.repository import OjTestCaseRepository
from app.modules.oj.problem.test_case.schema import (
    OjTestCaseAdminPageQuery,
    OjTestCaseCreateRequest,
    OjTestCaseSchema,
    OjTestCaseUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjTestCaseService:
    """OJ test case 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjTestCaseRepository(db)

    async def create(self, payload: OjTestCaseCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjTestCaseUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjTestCaseSchema:
        return to_schema(OjTestCaseSchema, await self.repo.get_required(query.id))

    async def page_admin(self, query: OjTestCaseAdminPageQuery) -> PageData[OjTestCaseSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjTestCaseSchema, items))
