from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.problem.tag_relation.repository import OjProblemTagRelationRepository
from app.modules.oj.problem.tag_relation.schema import (
    OjProblemTagRelationAdminPageQuery,
    OjProblemTagRelationCreateRequest,
    OjProblemTagRelationSchema,
    OjProblemTagRelationUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjProblemTagRelationService:
    """OJ problem tag relation 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjProblemTagRelationRepository(db)

    async def create(self, payload: OjProblemTagRelationCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjProblemTagRelationUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjProblemTagRelationSchema:
        return to_schema(OjProblemTagRelationSchema, await self.repo.get_required(query.id))

    async def page_admin(
        self, query: OjProblemTagRelationAdminPageQuery
    ) -> PageData[OjProblemTagRelationSchema]:
        items, total = await self.repo.page(query)
        return build_page(
            query.pagination, total, to_schema_list(OjProblemTagRelationSchema, items)
        )
