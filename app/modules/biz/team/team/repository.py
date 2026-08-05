"""Team repository."""

from datetime import datetime, timezone

from sqlalchemy import Select, func, select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.biz.team.enums import TeamScope, TeamStatus, TeamVisibility
from app.modules.biz.team.team.model import OjTeam, OjTeamMember
from app.modules.biz.team.team.schema import OjTeamAdminPageQuery


class OjTeamRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, entity: OjTeam) -> OjTeam:
        self.db.add(entity)
        await self.db.flush()
        return entity

    async def get_by_id(self, team_id: str) -> OjTeam | None:
        return await self.db.get(OjTeam, team_id)

    async def get_required(self, team_id: str) -> OjTeam:
        entity = await self.get_by_id(team_id)
        if entity is None:
            raise NotFoundError("小组不存在")
        return entity

    async def get_by_invite_code(self, invite_code: str) -> OjTeam | None:
        stmt = select(OjTeam).where(
            OjTeam.invite_code == invite_code,
            OjTeam.status == TeamStatus.ENABLED.value,
        )
        return (await self.db.execute(stmt)).scalar_one_or_none()

    async def invite_code_exists(self, invite_code: str) -> bool:
        stmt = select(OjTeam.id).where(OjTeam.invite_code == invite_code)
        return (await self.db.execute(stmt)).scalar_one_or_none() is not None

    async def update_entity(self, entity: OjTeam) -> None:
        await self.db.flush()

    async def page_admin(self, query: OjTeamAdminPageQuery) -> tuple[list[OjTeam], int]:
        stmt: Select[tuple[OjTeam]] = select(OjTeam)
        count_stmt = select(func.count(OjTeam.id))
        filters = []
        if query.scope is not None:
            filters.append(OjTeam.scope == query.scope.value)
        if query.course_id:
            filters.append(OjTeam.course_id == query.course_id)
        if query.name:
            filters.append(OjTeam.name.ilike(f"%{query.name}%"))
        if query.status is not None:
            filters.append(OjTeam.status == query.status.value)
        if query.visibility is not None:
            filters.append(OjTeam.visibility == query.visibility.value)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = stmt.order_by(OjTeam.id.desc()).offset(query.pagination.offset).limit(query.pagination.size)
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total

    async def list_my_teams(self, account_id: str) -> list[OjTeam]:
        stmt = (
            select(OjTeam)
            .join(OjTeamMember, OjTeamMember.team_id == OjTeam.id)
            .where(
                OjTeamMember.account_id == account_id,
                OjTeamMember.left_at.is_(None),
                OjTeam.status != TeamStatus.DISSOLVED.value,
            )
            .order_by(OjTeam.id.desc())
        )
        return list((await self.db.execute(stmt)).scalars().all())

    async def list_by_course(self, course_id: str) -> list[OjTeam]:
        stmt = (
            select(OjTeam)
            .where(
                OjTeam.course_id == course_id,
                OjTeam.scope == TeamScope.COURSE.value,
                OjTeam.status == TeamStatus.ENABLED.value,
            )
            .order_by(OjTeam.id.desc())
        )
        return list((await self.db.execute(stmt)).scalars().all())

    async def page_public_independent(
        self, keyword: str | None, offset: int, size: int
    ) -> tuple[list[OjTeam], int]:
        filters = [
            OjTeam.scope == TeamScope.INDEPENDENT.value,
            OjTeam.status == TeamStatus.ENABLED.value,
            OjTeam.visibility == TeamVisibility.PUBLIC.value,
        ]
        stmt: Select[tuple[OjTeam]] = select(OjTeam).where(*filters)
        count_stmt = select(func.count(OjTeam.id)).where(*filters)
        if keyword:
            like = f"%{keyword}%"
            kw = (OjTeam.name.ilike(like)) | (OjTeam.description.ilike(like))
            stmt = stmt.where(kw)
            count_stmt = count_stmt.where(kw)
        stmt = stmt.order_by(OjTeam.member_count.desc(), OjTeam.id.desc()).offset(offset).limit(size)
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total

    async def increment_member_count(self, team_id: str, delta: int = 1) -> None:
        team = await self.get_required(team_id)
        team.member_count += delta
        await self.db.flush()

    async def get_member(self, team_id: str, account_id: str) -> OjTeamMember | None:
        stmt = select(OjTeamMember).where(
            OjTeamMember.team_id == team_id,
            OjTeamMember.account_id == account_id,
            OjTeamMember.left_at.is_(None),
        )
        return (await self.db.execute(stmt)).scalar_one_or_none()

    async def list_members(self, team_id: str) -> list[OjTeamMember]:
        stmt = (
            select(OjTeamMember)
            .where(OjTeamMember.team_id == team_id, OjTeamMember.left_at.is_(None))
            .order_by(OjTeamMember.role.desc(), OjTeamMember.joined_at.asc())
        )
        return list((await self.db.execute(stmt)).scalars().all())

    async def add_member(self, team_id: str, account_id: str, role: str) -> OjTeamMember:
        now = datetime.now(timezone.utc)
        stmt = select(OjTeamMember).where(
            OjTeamMember.team_id == team_id,
            OjTeamMember.account_id == account_id,
        )
        existing = (await self.db.execute(stmt)).scalar_one_or_none()
        if existing is not None:
            existing.left_at = None
            existing.role = role
            existing.joined_at = now
            await self.db.flush()
            return existing
        member = OjTeamMember(team_id=team_id, account_id=account_id, role=role, joined_at=now)
        self.db.add(member)
        await self.db.flush()
        return member

    async def remove_member(self, team_id: str, account_id: str) -> None:
        now = datetime.now(timezone.utc)
        stmt = (
            update(OjTeamMember)
            .where(
                OjTeamMember.team_id == team_id,
                OjTeamMember.account_id == account_id,
                OjTeamMember.left_at.is_(None),
            )
            .values(left_at=now)
        )
        await self.db.execute(stmt)
