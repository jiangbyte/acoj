from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.submission.rejudge_record.repository import OjRejudgeRecordRepository
from app.modules.oj.submission.rejudge_record.schema import (
    OjRejudgeRecordAdminPageQuery,
    OjRejudgeRecordCreateRequest,
    OjRejudgeRecordSchema,
    OjRejudgeRecordUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjRejudgeRecordService:
    """OJ rejudge record 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjRejudgeRecordRepository(db)

    async def create(self, payload: OjRejudgeRecordCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjRejudgeRecordUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjRejudgeRecordSchema:
        return to_schema(OjRejudgeRecordSchema, await self.repo.get_required(query.id))

    async def page_admin(
        self, query: OjRejudgeRecordAdminPageQuery
    ) -> PageData[OjRejudgeRecordSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjRejudgeRecordSchema, items))
