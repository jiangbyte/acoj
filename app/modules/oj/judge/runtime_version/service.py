from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.judge.runtime_version.repository import OjRuntimeVersionRepository
from app.modules.oj.judge.runtime_version.schema import (
    OjRuntimeVersionAdminPageQuery,
    OjRuntimeVersionCreateRequest,
    OjRuntimeVersionSchema,
    OjRuntimeVersionUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjRuntimeVersionService:
    """OJ runtime version 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjRuntimeVersionRepository(db)

    async def create(self, payload: OjRuntimeVersionCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjRuntimeVersionUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjRuntimeVersionSchema:
        return to_schema(OjRuntimeVersionSchema, await self.repo.get_required(query.id))

    async def page_admin(
        self, query: OjRuntimeVersionAdminPageQuery
    ) -> PageData[OjRuntimeVersionSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjRuntimeVersionSchema, items))
