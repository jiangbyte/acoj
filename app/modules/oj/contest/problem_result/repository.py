from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.contest.problem_result.model import OjContestProblemResult
from app.modules.oj.contest.problem_result.schema import (
    OjContestProblemResultAdminPageQuery,
    OjContestProblemResultCreateRequest,
    OjContestProblemResultUpdateRequest,
)


class OjContestProblemResultRepository:
    """OJ contest problem result 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjContestProblemResultCreateRequest) -> None:
        entity = OjContestProblemResult(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjContestProblemResult | None:
        return await self.db.get(OjContestProblemResult, entity_id)

    async def get_required(self, entity_id: str) -> OjContestProblemResult:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ contest problem result not found")
        return entity

    async def update(self, payload: OjContestProblemResultUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjContestProblemResult.id).where(OjContestProblemResult.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ contest problem result not found")
        await self.db.execute(
            delete(OjContestProblemResult).where(OjContestProblemResult.id.in_(unique_ids))
        )

    async def page(
        self, query: OjContestProblemResultAdminPageQuery
    ) -> tuple[list[OjContestProblemResult], int]:
        stmt: Select[tuple[OjContestProblemResult]] = select(OjContestProblemResult)
        count_stmt = select(func.count(OjContestProblemResult.id))
        filters = []
        if query.contest_id:
            filters.append(OjContestProblemResult.contest_id == query.contest_id)
        if query.participation_id:
            filters.append(OjContestProblemResult.participation_id == query.participation_id)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjContestProblemResult.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
