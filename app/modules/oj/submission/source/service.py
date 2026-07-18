from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.submission.source.repository import OjSubmissionSourceRepository
from app.modules.oj.submission.source.schema import (
    OjSubmissionSourceAdminPageQuery,
    OjSubmissionSourceCreateRequest,
    OjSubmissionSourceSchema,
    OjSubmissionSourceUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjSubmissionSourceService:
    """OJ submission source 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjSubmissionSourceRepository(db)

    async def create(self, payload: OjSubmissionSourceCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjSubmissionSourceUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjSubmissionSourceSchema:
        return to_schema(OjSubmissionSourceSchema, await self.repo.get_required(query.id))

    async def page_admin(
        self, query: OjSubmissionSourceAdminPageQuery
    ) -> PageData[OjSubmissionSourceSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjSubmissionSourceSchema, items))
