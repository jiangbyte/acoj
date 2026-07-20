from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.problem.tag_relation.model import OjProblemTagRelation
from app.modules.oj.problem.tag_relation.schema import (
    OjProblemTagRelationAdminPageQuery,
    OjProblemTagRelationCreateRequest,
    OjProblemTagRelationUpdateRequest,
)


class OjProblemTagRelationRepository:
    """OJ problem tag relation 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjProblemTagRelationCreateRequest) -> None:
        entity = OjProblemTagRelation(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjProblemTagRelation | None:
        return await self.db.get(OjProblemTagRelation, entity_id)

    async def get_required(self, entity_id: str) -> OjProblemTagRelation:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ problem tag relation not found")
        return entity

    async def update(self, payload: OjProblemTagRelationUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjProblemTagRelation.id).where(OjProblemTagRelation.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ problem tag relation not found")
        await self.db.execute(
            delete(OjProblemTagRelation).where(OjProblemTagRelation.id.in_(unique_ids))
        )

    async def list_by_problem(self, problem_id: str) -> list[OjProblemTagRelation]:
        stmt = select(OjProblemTagRelation).where(OjProblemTagRelation.problem_id == problem_id)
        return list((await self.db.execute(stmt)).scalars().all())

    async def page(
        self, query: OjProblemTagRelationAdminPageQuery
    ) -> tuple[list[OjProblemTagRelation], int]:
        stmt: Select[tuple[OjProblemTagRelation]] = select(OjProblemTagRelation)
        count_stmt = select(func.count(OjProblemTagRelation.id))
        filters = []
        if query.problem_id:
            filters.append(OjProblemTagRelation.problem_id == query.problem_id)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjProblemTagRelation.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
