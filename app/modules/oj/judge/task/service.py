from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.judge.task.repository import OjJudgeTaskRepository
from app.modules.oj.judge.task.schema import (
    OjJudgeTaskAdminPageQuery,
    OjJudgeTaskCreateRequest,
    OjJudgeTaskSchema,
    OjJudgeTaskUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjJudgeTaskService:
    """OJ judge task 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjJudgeTaskRepository(db)

    async def create(self, payload: OjJudgeTaskCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjJudgeTaskUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjJudgeTaskSchema:
        return to_schema(OjJudgeTaskSchema, await self.repo.get_required(query.id))

    async def page_admin(self, query: OjJudgeTaskAdminPageQuery) -> PageData[OjJudgeTaskSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjJudgeTaskSchema, items))
