from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.problem.member.model import OjProblemMember
from app.modules.oj.problem.member.schema import (
    OjProblemMemberAdminPageQuery,
    OjProblemMemberCreateRequest,
    OjProblemMemberUpdateRequest,
)


class OjProblemMemberRepository:
    """OJ problem member 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjProblemMemberCreateRequest) -> None:
        entity = OjProblemMember(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjProblemMember | None:
        return await self.db.get(OjProblemMember, entity_id)

    async def get_required(self, entity_id: str) -> OjProblemMember:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ problem member not found")
        return entity

    async def update(self, payload: OjProblemMemberUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjProblemMember.id).where(OjProblemMember.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ problem member not found")
        await self.db.execute(delete(OjProblemMember).where(OjProblemMember.id.in_(unique_ids)))

    async def list_by_problem(self, problem_id: str) -> list[OjProblemMember]:
        stmt = select(OjProblemMember).where(OjProblemMember.problem_id == problem_id)
        return list((await self.db.execute(stmt)).scalars().all())

    async def page(self, query: OjProblemMemberAdminPageQuery) -> tuple[list[OjProblemMember], int]:
        stmt: Select[tuple[OjProblemMember]] = select(OjProblemMember)
        count_stmt = select(func.count(OjProblemMember.id))
        filters = []
        if query.problem_id:
            filters.append(OjProblemMember.problem_id == query.problem_id)
        if query.account_type:
            filters.append(OjProblemMember.account_type == query.account_type)
        if query.account_id:
            filters.append(OjProblemMember.account_id == query.account_id)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjProblemMember.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
