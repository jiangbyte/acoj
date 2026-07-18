from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.community.comment.repository import OjCommentRepository
from app.modules.oj.community.comment.schema import (
    OjCommentAdminPageQuery,
    OjCommentCreateRequest,
    OjCommentSchema,
    OjCommentUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjCommentService:
    """OJ comment 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjCommentRepository(db)

    async def create(self, payload: OjCommentCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjCommentUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjCommentSchema:
        return to_schema(OjCommentSchema, await self.repo.get_required(query.id))

    async def page_admin(self, query: OjCommentAdminPageQuery) -> PageData[OjCommentSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjCommentSchema, items))
