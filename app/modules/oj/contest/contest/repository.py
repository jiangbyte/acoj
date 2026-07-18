from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.contest.contest.model import OjContest
from app.modules.oj.contest.contest.schema import (
    OjContestAdminPageQuery,
    OjContestCreateRequest,
    OjContestUpdateRequest,
)


class OjContestRepository:
    """OJ contest 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjContestCreateRequest) -> None:
        entity = OjContest(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjContest | None:
        return await self.db.get(OjContest, entity_id)

    async def get_required(self, entity_id: str) -> OjContest:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ contest not found")
        return entity

    async def update(self, payload: OjContestUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjContest.id).where(OjContest.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ contest not found")
        await self.db.execute(delete(OjContest).where(OjContest.id.in_(unique_ids)))

    async def page(self, query: OjContestAdminPageQuery) -> tuple[list[OjContest], int]:
        stmt: Select[tuple[OjContest]] = select(OjContest)
        count_stmt = select(func.count(OjContest.id))
        filters = []
        if query.status:
            filters.append(OjContest.status == query.status)
        if query.key:
            filters.append(OjContest.key.contains(query.key))
        if query.name:
            filters.append(OjContest.name.contains(query.name))
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjContest.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
