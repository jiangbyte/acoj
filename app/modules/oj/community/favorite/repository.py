from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.community.favorite.model import OjFavorite
from app.modules.oj.community.favorite.schema import (
    OjFavoriteAdminPageQuery,
    OjFavoriteCreateRequest,
    OjFavoriteUpdateRequest,
)


class OjFavoriteRepository:
    """OJ favorite 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjFavoriteCreateRequest) -> None:
        entity = OjFavorite(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjFavorite | None:
        return await self.db.get(OjFavorite, entity_id)

    async def get_required(self, entity_id: str) -> OjFavorite:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ favorite not found")
        return entity

    async def update(self, payload: OjFavoriteUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjFavorite.id).where(OjFavorite.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ favorite not found")
        await self.db.execute(delete(OjFavorite).where(OjFavorite.id.in_(unique_ids)))

    async def page(self, query: OjFavoriteAdminPageQuery) -> tuple[list[OjFavorite], int]:
        stmt: Select[tuple[OjFavorite]] = select(OjFavorite)
        count_stmt = select(func.count(OjFavorite.id))
        filters = []
        if query.account_type:
            filters.append(OjFavorite.account_type == query.account_type)
        if query.account_id:
            filters.append(OjFavorite.account_id == query.account_id)
        if query.target_type:
            filters.append(OjFavorite.target_type == query.target_type)
        if query.target_id:
            filters.append(OjFavorite.target_id == query.target_id)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjFavorite.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
