from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.community.announcement.repository import OjAnnouncementRepository
from app.modules.oj.community.announcement.schema import (
    OjAnnouncementAdminPageQuery,
    OjAnnouncementCreateRequest,
    OjAnnouncementSchema,
    OjAnnouncementUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjAnnouncementService:
    """OJ announcement 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjAnnouncementRepository(db)

    async def create(self, payload: OjAnnouncementCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjAnnouncementUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjAnnouncementSchema:
        return to_schema(OjAnnouncementSchema, await self.repo.get_required(query.id))

    async def page_admin(
        self, query: OjAnnouncementAdminPageQuery
    ) -> PageData[OjAnnouncementSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjAnnouncementSchema, items))
