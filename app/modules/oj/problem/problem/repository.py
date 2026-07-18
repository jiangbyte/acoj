from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.problem.problem.model import OjProblem
from app.modules.oj.problem.problem.schema import (
    OjProblemAdminPageQuery,
    OjProblemCreateRequest,
    OjProblemUpdateRequest,
)


class OjProblemRepository:
    """OJ problem 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjProblemCreateRequest) -> None:
        entity = OjProblem(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjProblem | None:
        return await self.db.get(OjProblem, entity_id)

    async def get_required(self, entity_id: str) -> OjProblem:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ problem not found")
        return entity

    async def update(self, payload: OjProblemUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjProblem.id).where(OjProblem.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ problem not found")
        await self.db.execute(delete(OjProblem).where(OjProblem.id.in_(unique_ids)))

    async def page(self, query: OjProblemAdminPageQuery) -> tuple[list[OjProblem], int]:
        stmt: Select[tuple[OjProblem]] = select(OjProblem)
        count_stmt = select(func.count(OjProblem.id))
        filters = []
        if query.status:
            filters.append(OjProblem.status == query.status)
        if query.code:
            filters.append(OjProblem.code.contains(query.code))
        if query.title:
            filters.append(OjProblem.title.contains(query.title))
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjProblem.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
