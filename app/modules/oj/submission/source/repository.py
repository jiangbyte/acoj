from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.submission.source.model import OjSubmissionSource
from app.modules.oj.submission.source.schema import (
    OjSubmissionSourceAdminPageQuery,
    OjSubmissionSourceCreateRequest,
    OjSubmissionSourceUpdateRequest,
)


class OjSubmissionSourceRepository:
    """OJ submission source 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjSubmissionSourceCreateRequest) -> None:
        entity = OjSubmissionSource(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjSubmissionSource | None:
        return await self.db.get(OjSubmissionSource, entity_id)

    async def get_required(self, entity_id: str) -> OjSubmissionSource:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ submission source not found")
        return entity

    async def update(self, payload: OjSubmissionSourceUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjSubmissionSource.id).where(OjSubmissionSource.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ submission source not found")
        await self.db.execute(
            delete(OjSubmissionSource).where(OjSubmissionSource.id.in_(unique_ids))
        )

    async def page(
        self, query: OjSubmissionSourceAdminPageQuery
    ) -> tuple[list[OjSubmissionSource], int]:
        stmt: Select[tuple[OjSubmissionSource]] = select(OjSubmissionSource)
        count_stmt = select(func.count(OjSubmissionSource.id))
        filters = []
        if query.submission_id:
            filters.append(OjSubmissionSource.submission_id == query.submission_id)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjSubmissionSource.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
