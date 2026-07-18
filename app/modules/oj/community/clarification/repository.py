from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.community.clarification.model import OjClarification
from app.modules.oj.community.clarification.schema import (
    OjClarificationAdminPageQuery,
    OjClarificationCreateRequest,
    OjClarificationUpdateRequest,
)


class OjClarificationRepository:
    """OJ clarification 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjClarificationCreateRequest) -> None:
        entity = OjClarification(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjClarification | None:
        return await self.db.get(OjClarification, entity_id)

    async def get_required(self, entity_id: str) -> OjClarification:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ clarification not found")
        return entity

    async def update(self, payload: OjClarificationUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjClarification.id).where(OjClarification.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ clarification not found")
        await self.db.execute(delete(OjClarification).where(OjClarification.id.in_(unique_ids)))

    async def page(self, query: OjClarificationAdminPageQuery) -> tuple[list[OjClarification], int]:
        stmt: Select[tuple[OjClarification]] = select(OjClarification)
        count_stmt = select(func.count(OjClarification.id))
        filters = []
        if query.problem_id:
            filters.append(OjClarification.problem_id == query.problem_id)
        if query.contest_id:
            filters.append(OjClarification.contest_id == query.contest_id)
        if query.status:
            filters.append(OjClarification.status == query.status)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjClarification.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
