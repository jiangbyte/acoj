from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.contest.participation.model import OjContestParticipation
from app.modules.oj.contest.participation.schema import (
    OjContestParticipationAdminPageQuery,
    OjContestParticipationCreateRequest,
    OjContestParticipationUpdateRequest,
)


class OjContestParticipationRepository:
    """OJ contest participation 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjContestParticipationCreateRequest) -> None:
        entity = OjContestParticipation(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjContestParticipation | None:
        return await self.db.get(OjContestParticipation, entity_id)

    async def get_required(self, entity_id: str) -> OjContestParticipation:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ contest participation not found")
        return entity

    async def update(self, payload: OjContestParticipationUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjContestParticipation.id).where(OjContestParticipation.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ contest participation not found")
        await self.db.execute(
            delete(OjContestParticipation).where(OjContestParticipation.id.in_(unique_ids))
        )

    async def page(
        self, query: OjContestParticipationAdminPageQuery
    ) -> tuple[list[OjContestParticipation], int]:
        stmt: Select[tuple[OjContestParticipation]] = select(OjContestParticipation)
        count_stmt = select(func.count(OjContestParticipation.id))
        filters = []
        if query.contest_id:
            filters.append(OjContestParticipation.contest_id == query.contest_id)
        if query.account_type:
            filters.append(OjContestParticipation.account_type == query.account_type)
        if query.account_id:
            filters.append(OjContestParticipation.account_id == query.account_id)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjContestParticipation.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
