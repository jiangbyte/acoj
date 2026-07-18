from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.judge.node.repository import OjJudgeNodeRepository
from app.modules.oj.judge.node.schema import (
    OjJudgeNodeAdminPageQuery,
    OjJudgeNodeCreateRequest,
    OjJudgeNodeSchema,
    OjJudgeNodeUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjJudgeNodeService:
    """OJ judge node 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjJudgeNodeRepository(db)

    async def create(self, payload: OjJudgeNodeCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjJudgeNodeUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjJudgeNodeSchema:
        return to_schema(OjJudgeNodeSchema, await self.repo.get_required(query.id))

    async def page_admin(self, query: OjJudgeNodeAdminPageQuery) -> PageData[OjJudgeNodeSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjJudgeNodeSchema, items))
