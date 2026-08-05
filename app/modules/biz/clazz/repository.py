"""Class repository."""

from datetime import datetime, timezone

from sqlalchemy import Select, delete, func, select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.biz.clazz.enums import ClassStatus, ClassVisibility
from app.modules.biz.clazz.model import OjClass, OjClassMember
from app.modules.biz.clazz.schema import OjClassAdminPageQuery, OjClassCreateRequest, OjClassUpdateRequest


class OjClassRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, entity: OjClass) -> OjClass:
        self.db.add(entity)
        await self.db.flush()
        return entity

    async def get_by_id(self, entity_id: str) -> OjClass | None:
        return await self.db.get(OjClass, entity_id)

    async def get_required(self, entity_id: str) -> OjClass:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("班级不存在")
        return entity

    async def get_by_invite_code(self, invite_code: str) -> OjClass | None:
        stmt = select(OjClass).where(OjClass.invite_code == invite_code)
        return (await self.db.execute(stmt)).scalar_one_or_none()

    async def invite_code_exists(self, invite_code: str) -> bool:
        stmt = select(OjClass.id).where(OjClass.invite_code == invite_code)
        return (await self.db.execute(stmt)).scalar_one_or_none() is not None

    async def update(self, payload: OjClassUpdateRequest) -> OjClass:
        entity = await self.get_required(payload.id)
        for key, value in payload.model_dump(exclude={"id"}, exclude_none=True).items():
            setattr(entity, key, value.value if hasattr(value, "value") else value)
        await self.db.flush()
        return entity

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        stmt = select(OjClass.id).where(OjClass.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("班级不存在")
        await self.db.execute(delete(OjClass).where(OjClass.id.in_(unique_ids)))

    async def page_admin(self, query: OjClassAdminPageQuery) -> tuple[list[OjClass], int]:
        stmt: Select[tuple[OjClass]] = select(OjClass)
        count_stmt = select(func.count(OjClass.id))
        filters = []
        if query.code:
            filters.append(OjClass.code.ilike(f"%{query.code}%"))
        if query.name:
            filters.append(OjClass.name.ilike(f"%{query.name}%"))
        if query.status is not None:
            filters.append(OjClass.status == query.status.value)
        if query.visibility is not None:
            filters.append(OjClass.visibility == query.visibility.value)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = stmt.order_by(OjClass.id.desc()).offset(query.pagination.offset).limit(query.pagination.size)
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total

    async def page_public(self, keyword: str | None, offset: int, size: int) -> tuple[list[OjClass], int]:
        stmt: Select[tuple[OjClass]] = select(OjClass).where(
            OjClass.status == ClassStatus.ENABLED.value,
            OjClass.visibility == ClassVisibility.PUBLIC.value,
        )
        count_stmt = select(func.count(OjClass.id)).where(
            OjClass.status == ClassStatus.ENABLED.value,
            OjClass.visibility == ClassVisibility.PUBLIC.value,
        )
        if keyword:
            like = f"%{keyword}%"
            filt = (OjClass.name.ilike(like)) | (OjClass.code.ilike(like)) | (OjClass.summary.ilike(like))
            stmt = stmt.where(filt)
            count_stmt = count_stmt.where(filt)
        stmt = stmt.order_by(OjClass.member_count.desc(), OjClass.id.desc()).offset(offset).limit(size)
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total

    async def increment_member_count(self, class_id: str, delta: int = 1) -> None:
        clazz = await self.get_required(class_id)
        clazz.member_count += delta
        await self.db.flush()

    # --- members ---

    async def get_member(self, class_id: str, account_id: str) -> OjClassMember | None:
        stmt = select(OjClassMember).where(
            OjClassMember.class_id == class_id,
            OjClassMember.account_id == account_id,
            OjClassMember.left_at.is_(None),
        )
        return (await self.db.execute(stmt)).scalar_one_or_none()

    async def list_members(self, class_id: str) -> list[OjClassMember]:
        stmt = (
            select(OjClassMember)
            .where(OjClassMember.class_id == class_id, OjClassMember.left_at.is_(None))
            .order_by(OjClassMember.joined_at.asc())
        )
        return list((await self.db.execute(stmt)).scalars().all())

    async def list_my_classes(self, account_id: str) -> list[OjClass]:
        stmt = (
            select(OjClass)
            .join(OjClassMember, OjClassMember.class_id == OjClass.id)
            .where(
                OjClassMember.account_id == account_id,
                OjClassMember.left_at.is_(None),
                OjClass.status == ClassStatus.ENABLED.value,
            )
            .order_by(OjClass.id.desc())
        )
        return list((await self.db.execute(stmt)).scalars().all())

    async def add_member(self, class_id: str, account_id: str, role: str) -> OjClassMember:
        now = datetime.now(timezone.utc)
        stmt = select(OjClassMember).where(
            OjClassMember.class_id == class_id,
            OjClassMember.account_id == account_id,
        )
        existing = (await self.db.execute(stmt)).scalar_one_or_none()
        if existing is not None:
            existing.left_at = None
            existing.role = role
            existing.joined_at = now
            await self.db.flush()
            return existing
        member = OjClassMember(
            class_id=class_id,
            account_id=account_id,
            role=role,
            joined_at=now,
        )
        self.db.add(member)
        await self.db.flush()
        return member

    async def remove_member(self, class_id: str, account_id: str) -> None:
        now = datetime.now(timezone.utc)
        stmt = (
            update(OjClassMember)
            .where(
                OjClassMember.class_id == class_id,
                OjClassMember.account_id == account_id,
                OjClassMember.left_at.is_(None),
            )
            .values(left_at=now)
        )
        await self.db.execute(stmt)

    async def is_member(self, class_id: str, account_id: str) -> bool:
        return (await self.get_member(class_id, account_id)) is not None
