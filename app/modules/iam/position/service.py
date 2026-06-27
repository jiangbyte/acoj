from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.iam.position.repository import PositionRepository
from app.modules.iam.position.schema import (
    PositionAdminPageQuery,
    PositionCreateRequest,
    PositionUpdateRequest,
    SysPositionSchema,
)
from app.platform.db.transaction import transactional


class PositionService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = PositionRepository(db)

    async def create(self, payload: PositionCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: PositionUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> SysPositionSchema:
        return to_schema(SysPositionSchema, await self.repo.get_required(query.id))

    async def page_admin(self, query: PositionAdminPageQuery) -> PageData[SysPositionSchema]:
        items, total = await self.repo.page_admin(query)
        return build_page(query.pagination, total, to_schema_list(SysPositionSchema, items))
