from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.community.solution.model import OjSolution
from app.modules.oj.community.solution.schema import (
    OjSolutionAdminPageQuery,
    OjSolutionCreateRequest,
    OjSolutionUpdateRequest,
)


class OjSolutionRepository:
    """OJ solution 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjSolutionCreateRequest) -> None:
        entity = OjSolution(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjSolution | None:
        return await self.db.get(OjSolution, entity_id)

    async def get_required(self, entity_id: str) -> OjSolution:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ solution not found")
        return entity

    async def update(self, payload: OjSolutionUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjSolution.id).where(OjSolution.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ solution not found")
        await self.db.execute(delete(OjSolution).where(OjSolution.id.in_(unique_ids)))

    async def page(self, query: OjSolutionAdminPageQuery) -> tuple[list[OjSolution], int]:
        stmt: Select[tuple[OjSolution]] = select(OjSolution)
        count_stmt = select(func.count(OjSolution.id))
        filters = []
        if query.problem_id:
            filters.append(OjSolution.problem_id == query.problem_id)
        if query.account_type:
            filters.append(OjSolution.account_type == query.account_type)
        if query.account_id:
            filters.append(OjSolution.account_id == query.account_id)
        if query.status:
            filters.append(OjSolution.status == query.status)
        if query.title:
            filters.append(OjSolution.title.contains(query.title))
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjSolution.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
