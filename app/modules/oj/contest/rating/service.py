from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.contest.rating.repository import OjContestRatingRepository
from app.modules.oj.contest.rating.schema import (
    OjContestRatingAdminPageQuery,
    OjContestRatingCreateRequest,
    OjContestRatingSchema,
    OjContestRatingUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjContestRatingService:
    """OJ contest rating 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjContestRatingRepository(db)

    async def create(self, payload: OjContestRatingCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjContestRatingUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjContestRatingSchema:
        return to_schema(OjContestRatingSchema, await self.repo.get_required(query.id))

    async def page_admin(
        self, query: OjContestRatingAdminPageQuery
    ) -> PageData[OjContestRatingSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjContestRatingSchema, items))
