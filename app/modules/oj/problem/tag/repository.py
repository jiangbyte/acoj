from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.problem.tag.model import OjProblemTag
from app.modules.oj.problem.tag.schema import (
    OjProblemTagAdminPageQuery,
    OjProblemTagCreateRequest,
    OjProblemTagUpdateRequest,
)


class OjProblemTagRepository:
    """OJ problem tag 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjProblemTagCreateRequest) -> None:
        entity = OjProblemTag(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjProblemTag | None:
        return await self.db.get(OjProblemTag, entity_id)

    async def get_required(self, entity_id: str) -> OjProblemTag:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ problem tag not found")
        return entity

    async def update(self, payload: OjProblemTagUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjProblemTag.id).where(OjProblemTag.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ problem tag not found")
        await self.db.execute(delete(OjProblemTag).where(OjProblemTag.id.in_(unique_ids)))

    async def page(self, query: OjProblemTagAdminPageQuery) -> tuple[list[OjProblemTag], int]:
        stmt: Select[tuple[OjProblemTag]] = select(OjProblemTag)
        count_stmt = select(func.count(OjProblemTag.id))
        filters = []
        if query.status:
            filters.append(OjProblemTag.status == query.status)
        if query.code:
            filters.append(OjProblemTag.code.contains(query.code))
        if query.name:
            filters.append(OjProblemTag.name.contains(query.name))
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjProblemTag.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
