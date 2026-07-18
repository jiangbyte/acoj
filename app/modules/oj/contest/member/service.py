from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.contest.member.repository import OjContestMemberRepository
from app.modules.oj.contest.member.schema import (
    OjContestMemberAdminPageQuery,
    OjContestMemberCreateRequest,
    OjContestMemberSchema,
    OjContestMemberUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjContestMemberService:
    """OJ contest member 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjContestMemberRepository(db)

    async def create(self, payload: OjContestMemberCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjContestMemberUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjContestMemberSchema:
        return to_schema(OjContestMemberSchema, await self.repo.get_required(query.id))

    async def page_admin(
        self, query: OjContestMemberAdminPageQuery
    ) -> PageData[OjContestMemberSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjContestMemberSchema, items))
