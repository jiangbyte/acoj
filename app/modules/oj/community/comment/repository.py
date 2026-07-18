from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.community.comment.model import OjComment
from app.modules.oj.community.comment.schema import (
    OjCommentAdminPageQuery,
    OjCommentCreateRequest,
    OjCommentUpdateRequest,
)


class OjCommentRepository:
    """OJ comment 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjCommentCreateRequest) -> None:
        entity = OjComment(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjComment | None:
        return await self.db.get(OjComment, entity_id)

    async def get_required(self, entity_id: str) -> OjComment:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ comment not found")
        return entity

    async def update(self, payload: OjCommentUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjComment.id).where(OjComment.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ comment not found")
        await self.db.execute(delete(OjComment).where(OjComment.id.in_(unique_ids)))

    async def page(self, query: OjCommentAdminPageQuery) -> tuple[list[OjComment], int]:
        stmt: Select[tuple[OjComment]] = select(OjComment)
        count_stmt = select(func.count(OjComment.id))
        filters = []
        if query.account_type:
            filters.append(OjComment.account_type == query.account_type)
        if query.account_id:
            filters.append(OjComment.account_id == query.account_id)
        if query.target_type:
            filters.append(OjComment.target_type == query.target_type)
        if query.target_id:
            filters.append(OjComment.target_id == query.target_id)
        if query.status:
            filters.append(OjComment.status == query.status)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjComment.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
