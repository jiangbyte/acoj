from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.oj.community.announcement.model import OjAnnouncement
from app.modules.oj.community.announcement.schema import (
    OjAnnouncementAdminPageQuery,
    OjAnnouncementCreateRequest,
    OjAnnouncementUpdateRequest,
)


class OjAnnouncementRepository:
    """OJ announcement 仓储，负责直接持久化和分页查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: OjAnnouncementCreateRequest) -> None:
        entity = OjAnnouncement(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()

    async def get_by_id(self, entity_id: str) -> OjAnnouncement | None:
        return await self.db.get(OjAnnouncement, entity_id)

    async def get_required(self, entity_id: str) -> OjAnnouncement:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OJ announcement not found")
        return entity

    async def update(self, payload: OjAnnouncementUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjAnnouncement.id).where(OjAnnouncement.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("OJ announcement not found")
        await self.db.execute(delete(OjAnnouncement).where(OjAnnouncement.id.in_(unique_ids)))

    async def page(self, query: OjAnnouncementAdminPageQuery) -> tuple[list[OjAnnouncement], int]:
        stmt: Select[tuple[OjAnnouncement]] = select(OjAnnouncement)
        count_stmt = select(func.count(OjAnnouncement.id))
        filters = []
        if query.contest_id:
            filters.append(OjAnnouncement.contest_id == query.contest_id)
        if query.status:
            filters.append(OjAnnouncement.status == query.status)
        if query.title:
            filters.append(OjAnnouncement.title.contains(query.title))
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjAnnouncement.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total
