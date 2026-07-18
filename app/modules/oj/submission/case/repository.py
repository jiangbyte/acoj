from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.submission.case.model import OjSubmissionCase
from app.modules.oj.submission.case.schema import (
    OjSubmissionCaseAdminPageQuery,
    OjSubmissionCaseCreateRequest,
    OjSubmissionCaseUpdateRequest,
)


class OjSubmissionCaseRepository:
    """OJ submission case 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjSubmissionCaseCreateRequest) -> None:
        entity = OjSubmissionCase(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjSubmissionCase | None:
        return await self.db.get(OjSubmissionCase, entity_id)

    async def get_required(self, entity_id: str) -> OjSubmissionCase:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ submission case not found")
        return entity

    async def update(self, payload: OjSubmissionCaseUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjSubmissionCase.id).where(OjSubmissionCase.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ submission case not found")
        await self.db.execute(delete(OjSubmissionCase).where(OjSubmissionCase.id.in_(unique_ids)))

    async def page(
        self, query: OjSubmissionCaseAdminPageQuery
    ) -> tuple[list[OjSubmissionCase], int]:
        stmt: Select[tuple[OjSubmissionCase]] = select(OjSubmissionCase)
        count_stmt = select(func.count(OjSubmissionCase.id))
        filters = []
        if query.submission_id:
            filters.append(OjSubmissionCase.submission_id == query.submission_id)
        if query.status:
            filters.append(OjSubmissionCase.status == query.status)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjSubmissionCase.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
