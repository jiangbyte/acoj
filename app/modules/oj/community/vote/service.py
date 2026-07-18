from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.community.vote.repository import OjVoteRepository
from app.modules.oj.community.vote.schema import (
    OjVoteAdminPageQuery,
    OjVoteCreateRequest,
    OjVoteSchema,
    OjVoteUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjVoteService:
    """OJ vote 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjVoteRepository(db)

    async def create(self, payload: OjVoteCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjVoteUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjVoteSchema:
        return to_schema(OjVoteSchema, await self.repo.get_required(query.id))

    async def page_admin(self, query: OjVoteAdminPageQuery) -> PageData[OjVoteSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjVoteSchema, items))
