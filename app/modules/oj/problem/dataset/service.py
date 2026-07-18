from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.problem.dataset.repository import OjDatasetRepository
from app.modules.oj.problem.dataset.schema import (
    OjDatasetAdminPageQuery,
    OjDatasetCreateRequest,
    OjDatasetSchema,
    OjDatasetUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjDatasetService:
    """OJ dataset 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjDatasetRepository(db)

    async def create(self, payload: OjDatasetCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjDatasetUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjDatasetSchema:
        return to_schema(OjDatasetSchema, await self.repo.get_required(query.id))

    async def page_admin(self, query: OjDatasetAdminPageQuery) -> PageData[OjDatasetSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjDatasetSchema, items))
