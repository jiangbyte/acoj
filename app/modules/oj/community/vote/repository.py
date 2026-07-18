from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.community.vote.model import OjVote
from app.modules.oj.community.vote.schema import (
    OjVoteAdminPageQuery,
    OjVoteCreateRequest,
    OjVoteUpdateRequest,
)


class OjVoteRepository:
    """OJ vote 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjVoteCreateRequest) -> None:
        entity = OjVote(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjVote | None:
        return await self.db.get(OjVote, entity_id)

    async def get_required(self, entity_id: str) -> OjVote:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ vote not found")
        return entity

    async def update(self, payload: OjVoteUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjVote.id).where(OjVote.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ vote not found")
        await self.db.execute(delete(OjVote).where(OjVote.id.in_(unique_ids)))

    async def page(self, query: OjVoteAdminPageQuery) -> tuple[list[OjVote], int]:
        stmt: Select[tuple[OjVote]] = select(OjVote)
        count_stmt = select(func.count(OjVote.id))
        filters = []
        if query.account_type:
            filters.append(OjVote.account_type == query.account_type)
        if query.account_id:
            filters.append(OjVote.account_id == query.account_id)
        if query.target_type:
            filters.append(OjVote.target_type == query.target_type)
        if query.target_id:
            filters.append(OjVote.target_id == query.target_id)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjVote.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
