from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.judge.language.model import OjLanguage
from app.modules.oj.judge.language.schema import (
    OjLanguageAdminPageQuery,
    OjLanguageCreateRequest,
    OjLanguageUpdateRequest,
)


class OjLanguageRepository:
    """OJ language 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjLanguageCreateRequest) -> None:
        entity = OjLanguage(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjLanguage | None:
        return await self.db.get(OjLanguage, entity_id)

    async def get_required(self, entity_id: str) -> OjLanguage:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ language not found")
        return entity

    async def update(self, payload: OjLanguageUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjLanguage.id).where(OjLanguage.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ language not found")
        await self.db.execute(delete(OjLanguage).where(OjLanguage.id.in_(unique_ids)))

    async def page(self, query: OjLanguageAdminPageQuery) -> tuple[list[OjLanguage], int]:
        stmt: Select[tuple[OjLanguage]] = select(OjLanguage)
        count_stmt = select(func.count(OjLanguage.id))
        filters = []
        if query.status:
            filters.append(OjLanguage.status == query.status)
        if query.key:
            filters.append(OjLanguage.key.contains(query.key))
        if query.name:
            filters.append(OjLanguage.name.contains(query.name))
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjLanguage.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
