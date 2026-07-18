from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.contest.rating.model import OjContestRating
from app.modules.oj.contest.rating.schema import (
    OjContestRatingAdminPageQuery,
    OjContestRatingCreateRequest,
    OjContestRatingUpdateRequest,
)


class OjContestRatingRepository:
    """OJ contest rating 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjContestRatingCreateRequest) -> None:
        entity = OjContestRating(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjContestRating | None:
        return await self.db.get(OjContestRating, entity_id)

    async def get_required(self, entity_id: str) -> OjContestRating:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ contest rating not found")
        return entity

    async def update(self, payload: OjContestRatingUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjContestRating.id).where(OjContestRating.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ contest rating not found")
        await self.db.execute(delete(OjContestRating).where(OjContestRating.id.in_(unique_ids)))

    async def page(self, query: OjContestRatingAdminPageQuery) -> tuple[list[OjContestRating], int]:
        stmt: Select[tuple[OjContestRating]] = select(OjContestRating)
        count_stmt = select(func.count(OjContestRating.id))
        filters = []
        if query.contest_id:
            filters.append(OjContestRating.contest_id == query.contest_id)
        if query.participation_id:
            filters.append(OjContestRating.participation_id == query.participation_id)
        if query.account_type:
            filters.append(OjContestRating.account_type == query.account_type)
        if query.account_id:
            filters.append(OjContestRating.account_id == query.account_id)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjContestRating.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
