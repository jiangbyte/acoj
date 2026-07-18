from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.sys.config.repository import ConfigRepository
from app.modules.sys.config.schema import (
    ConfigAdminPageQuery,
    ConfigCreateRequest,
    ConfigUpdateRequest,
    SysConfigSchema,
)
from app.platform.db.transaction import transactional


class ConfigService:
    """系统配置服务，负责管理端配置维护。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = ConfigRepository(db)

    async def create(self, payload: ConfigCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: ConfigUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> SysConfigSchema:
        return to_schema(SysConfigSchema, await self.repo.get_required(query.id))

    async def page_admin(self, query: ConfigAdminPageQuery) -> PageData[SysConfigSchema]:
        items, total = await self.repo.page_admin(query)
        return build_page(query.pagination, total, to_schema_list(SysConfigSchema, items))
