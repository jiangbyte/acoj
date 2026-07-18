from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.submission.submission.model import OjSubmission
from app.modules.oj.submission.submission.schema import (
    OjSubmissionAdminPageQuery,
    OjSubmissionCreateRequest,
    OjSubmissionUpdateRequest,
)


class OjSubmissionRepository:
    """OJ submission 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjSubmissionCreateRequest) -> None:
        entity = OjSubmission(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjSubmission | None:
        return await self.db.get(OjSubmission, entity_id)

    async def get_required(self, entity_id: str) -> OjSubmission:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ submission not found")
        return entity

    async def update(self, payload: OjSubmissionUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjSubmission.id).where(OjSubmission.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ submission not found")
        await self.db.execute(delete(OjSubmission).where(OjSubmission.id.in_(unique_ids)))

    async def page(self, query: OjSubmissionAdminPageQuery) -> tuple[list[OjSubmission], int]:
        stmt: Select[tuple[OjSubmission]] = select(OjSubmission)
        count_stmt = select(func.count(OjSubmission.id))
        filters = []
        if query.problem_id:
            filters.append(OjSubmission.problem_id == query.problem_id)
        if query.contest_id:
            filters.append(OjSubmission.contest_id == query.contest_id)
        if query.participation_id:
            filters.append(OjSubmission.participation_id == query.participation_id)
        if query.account_type:
            filters.append(OjSubmission.account_type == query.account_type)
        if query.account_id:
            filters.append(OjSubmission.account_id == query.account_id)
        if query.status:
            filters.append(OjSubmission.status == query.status)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjSubmission.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
