from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.problem.objective_answer.model import OjObjectiveAnswer
from app.modules.oj.problem.objective_answer.schema import (
    OjObjectiveAnswerAdminPageQuery,
    OjObjectiveAnswerCreateRequest,
    OjObjectiveAnswerUpdateRequest,
)


class OjObjectiveAnswerRepository:
    """OJ objective answer 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjObjectiveAnswerCreateRequest) -> None:
        entity = OjObjectiveAnswer(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjObjectiveAnswer | None:
        return await self.db.get(OjObjectiveAnswer, entity_id)

    async def get_required(self, entity_id: str) -> OjObjectiveAnswer:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ objective answer not found")
        return entity

    async def update(self, payload: OjObjectiveAnswerUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjObjectiveAnswer.id).where(OjObjectiveAnswer.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ objective answer not found")
        await self.db.execute(delete(OjObjectiveAnswer).where(OjObjectiveAnswer.id.in_(unique_ids)))

    async def page(
        self, query: OjObjectiveAnswerAdminPageQuery
    ) -> tuple[list[OjObjectiveAnswer], int]:
        stmt: Select[tuple[OjObjectiveAnswer]] = select(OjObjectiveAnswer)
        count_stmt = select(func.count(OjObjectiveAnswer.id))
        filters = []
        if query.problem_id:
            filters.append(OjObjectiveAnswer.problem_id == query.problem_id)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjObjectiveAnswer.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
