from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.contest.problem.model import OjContestProblem
from app.modules.oj.contest.problem.schema import (
    OjContestProblemAdminPageQuery,
    OjContestProblemCreateRequest,
    OjContestProblemUpdateRequest,
)


class OjContestProblemRepository:
    """OJ contest problem 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjContestProblemCreateRequest) -> None:
        entity = OjContestProblem(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjContestProblem | None:
        return await self.db.get(OjContestProblem, entity_id)

    async def get_required(self, entity_id: str) -> OjContestProblem:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ contest problem not found")
        return entity

    async def update(self, payload: OjContestProblemUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjContestProblem.id).where(OjContestProblem.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ contest problem not found")
        await self.db.execute(delete(OjContestProblem).where(OjContestProblem.id.in_(unique_ids)))

    async def page(
        self, query: OjContestProblemAdminPageQuery
    ) -> tuple[list[OjContestProblem], int]:
        stmt: Select[tuple[OjContestProblem]] = select(OjContestProblem)
        count_stmt = select(func.count(OjContestProblem.id))
        filters = []
        if query.problem_id:
            filters.append(OjContestProblem.problem_id == query.problem_id)
        if query.contest_id:
            filters.append(OjContestProblem.contest_id == query.contest_id)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjContestProblem.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
