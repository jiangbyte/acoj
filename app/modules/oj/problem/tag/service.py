from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.problem.tag.repository import OjProblemTagRepository
from app.modules.oj.problem.tag.schema import (
    OjProblemTagAdminPageQuery,
    OjProblemTagCreateRequest,
    OjProblemTagSchema,
    OjProblemTagUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjProblemTagService:
    """OJ problem tag 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjProblemTagRepository(db)

    async def create(self, payload: OjProblemTagCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjProblemTagUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjProblemTagSchema:
        return to_schema(OjProblemTagSchema, await self.repo.get_required(query.id))

    async def page_admin(self, query: OjProblemTagAdminPageQuery) -> PageData[OjProblemTagSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjProblemTagSchema, items))
