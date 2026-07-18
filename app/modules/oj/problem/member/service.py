from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.problem.member.repository import OjProblemMemberRepository
from app.modules.oj.problem.member.schema import (
    OjProblemMemberAdminPageQuery,
    OjProblemMemberCreateRequest,
    OjProblemMemberSchema,
    OjProblemMemberUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjProblemMemberService:
    """OJ problem member 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjProblemMemberRepository(db)

    async def create(self, payload: OjProblemMemberCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjProblemMemberUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjProblemMemberSchema:
        return to_schema(OjProblemMemberSchema, await self.repo.get_required(query.id))

    async def page_admin(
        self, query: OjProblemMemberAdminPageQuery
    ) -> PageData[OjProblemMemberSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjProblemMemberSchema, items))
