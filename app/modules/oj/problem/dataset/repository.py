from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.problem.dataset.model import OjDataset
from app.modules.oj.problem.dataset.schema import (
    OjDatasetAdminPageQuery,
    OjDatasetCreateRequest,
    OjDatasetUpdateRequest,
)


class OjDatasetRepository:
    """OJ dataset 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjDatasetCreateRequest) -> None:
        entity = OjDataset(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjDataset | None:
        return await self.db.get(OjDataset, entity_id)

    async def get_required(self, entity_id: str) -> OjDataset:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ dataset not found")
        return entity

    async def update(self, payload: OjDatasetUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjDataset.id).where(OjDataset.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ dataset not found")
        await self.db.execute(delete(OjDataset).where(OjDataset.id.in_(unique_ids)))

    async def list_by_problem(self, problem_id: str) -> list[OjDataset]:
        stmt = select(OjDataset).where(OjDataset.problem_id == problem_id)
        return list((await self.db.execute(stmt)).scalars().all())

    async def page(self, query: OjDatasetAdminPageQuery) -> tuple[list[OjDataset], int]:
        stmt: Select[tuple[OjDataset]] = select(OjDataset)
        count_stmt = select(func.count(OjDataset.id))
        filters = []
        if query.problem_id:
            filters.append(OjDataset.problem_id == query.problem_id)
        if query.name:
            filters.append(OjDataset.name.contains(query.name))
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjDataset.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
