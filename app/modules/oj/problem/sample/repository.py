from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.problem.sample.model import OjProblemSample
from app.modules.oj.problem.sample.schema import (
    OjProblemSampleAdminPageQuery,
    OjProblemSampleCreateRequest,
    OjProblemSampleUpdateRequest,
)


class OjProblemSampleRepository:
    """OJ problem sample 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjProblemSampleCreateRequest) -> None:
        entity = OjProblemSample(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjProblemSample | None:
        return await self.db.get(OjProblemSample, entity_id)

    async def get_required(self, entity_id: str) -> OjProblemSample:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ problem sample not found")
        return entity

    async def update(self, payload: OjProblemSampleUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjProblemSample.id).where(OjProblemSample.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ problem sample not found")
        await self.db.execute(delete(OjProblemSample).where(OjProblemSample.id.in_(unique_ids)))

    async def page(self, query: OjProblemSampleAdminPageQuery) -> tuple[list[OjProblemSample], int]:
        stmt: Select[tuple[OjProblemSample]] = select(OjProblemSample)
        count_stmt = select(func.count(OjProblemSample.id))
        filters = []
        if query.problem_id:
            filters.append(OjProblemSample.problem_id == query.problem_id)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjProblemSample.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
