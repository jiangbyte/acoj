from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.problem.asset.model import OjProblemAsset
from app.modules.oj.problem.asset.schema import (
    OjProblemAssetAdminPageQuery,
    OjProblemAssetCreateRequest,
    OjProblemAssetUpdateRequest,
)


class OjProblemAssetRepository:
    """OJ problem asset 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjProblemAssetCreateRequest) -> None:
        entity = OjProblemAsset(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjProblemAsset | None:
        return await self.db.get(OjProblemAsset, entity_id)

    async def get_required(self, entity_id: str) -> OjProblemAsset:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ problem asset not found")
        return entity

    async def update(self, payload: OjProblemAssetUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjProblemAsset.id).where(OjProblemAsset.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ problem asset not found")
        await self.db.execute(delete(OjProblemAsset).where(OjProblemAsset.id.in_(unique_ids)))

    async def page(self, query: OjProblemAssetAdminPageQuery) -> tuple[list[OjProblemAsset], int]:
        stmt: Select[tuple[OjProblemAsset]] = select(OjProblemAsset)
        count_stmt = select(func.count(OjProblemAsset.id))
        filters = []
        if query.problem_id:
            filters.append(OjProblemAsset.problem_id == query.problem_id)
        if query.name:
            filters.append(OjProblemAsset.name.contains(query.name))
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjProblemAsset.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
