"""Contest registration business logic (admin + portal)."""

from __future__ import annotations

from sqlalchemy import and_, func, or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import BusinessError, NotFoundError
from app.core.response.pagination import PageData, build_page
from app.core.schema.base import to_schema, to_schema_list
from app.modules.biz.contest.banned_user.model import OjContestBannedUser
from app.modules.biz.contest.contest.model import OjContest
from app.modules.biz.contest.enums import (
    ContestLifecycleStatus,
    ContestListVisibility,
    ContestParticipationVirtual,
    ContestRegistrationMode,
    ContestRegistrationSource,
    ContestRegistrationStatus,
)
from app.modules.biz.contest.lifecycle import lifecycle_status, utcnow
from app.modules.biz.contest.participation.model import OjContestParticipation
from app.modules.biz.contest.problem.model import OjContestProblem
from app.modules.biz.contest.registration.model import OjContestRegistration
from app.modules.biz.contest.registration.schema import (
    OjContestRegistrationAddRequest,
    OjContestRegistrationAdminPageQuery,
    OjContestRegistrationIdsRequest,
    OjContestRegistrationRejectRequest,
    OjContestRegistrationSchema,
)
from app.platform.db.transaction import transactional
from app.platform.id_generator.snowflake import generate_snowflake_id


class ContestRegistrationService:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_by_user(self, contest_id: str, account_id: str) -> OjContestRegistration | None:
        return (
            await self.db.execute(
                select(OjContestRegistration).where(
                    OjContestRegistration.contest_id == contest_id,
                    OjContestRegistration.account_id == account_id,
                )
            )
        ).scalar_one_or_none()

    async def is_approved(self, contest_id: str, account_id: str) -> bool:
        row = await self.get_by_user(contest_id, account_id)
        return row is not None and row.status == ContestRegistrationStatus.APPROVED

    async def ensure_approved(self, contest_id: str, account_id: str) -> OjContestRegistration:
        row = await self.get_by_user(contest_id, account_id)
        if row is None or row.status != ContestRegistrationStatus.APPROVED:
            raise BusinessError("未获准参赛，无法进行此操作")
        return row

    async def page_admin(self, query: OjContestRegistrationAdminPageQuery) -> PageData[OjContestRegistrationSchema]:
        filters = []
        if query.contest_id:
            filters.append(OjContestRegistration.contest_id == query.contest_id)
        if query.account_id:
            filters.append(OjContestRegistration.account_id == query.account_id)
        if query.status:
            filters.append(OjContestRegistration.status == query.status.value)

        count_stmt = select(func.count(OjContestRegistration.id))
        stmt = select(OjContestRegistration).order_by(OjContestRegistration.applied_at.desc())
        if filters:
            count_stmt = count_stmt.where(*filters)
            stmt = stmt.where(*filters)
        total = int((await self.db.execute(count_stmt)).scalar_one() or 0)
        rows = list(
            (
                await self.db.execute(
                    stmt.offset(query.pagination.offset).limit(query.pagination.size)
                )
            )
            .scalars()
            .all()
        )
        return build_page(query.pagination, total, to_schema_list(OjContestRegistrationSchema, rows))

    async def add(self, payload: OjContestRegistrationAddRequest, reviewer_id: str | None) -> str:
        contest_id = payload.contest_id
        contest = await self.db.get(OjContest, contest_id)
        if contest is None:
            raise NotFoundError("竞赛不存在")
        async with transactional(self.db):
            existing = await self.get_by_user(contest_id, payload.account_id)
            now = utcnow()
            if existing:
                existing.status = ContestRegistrationStatus.APPROVED
                existing.source = ContestRegistrationSource.ADMIN
                existing.reviewed_at = now
                existing.reviewed_by = reviewer_id
                existing.remark = payload.remark
                await self.db.flush()
                return existing.id
            entity = OjContestRegistration(
                id=generate_snowflake_id(),
                contest_id=contest_id,
                account_id=payload.account_id,
                status=ContestRegistrationStatus.APPROVED,
                source=ContestRegistrationSource.ADMIN,
                applied_at=now,
                reviewed_at=now,
                reviewed_by=reviewer_id,
                remark=payload.remark,
            )
            self.db.add(entity)
            await self.db.flush()
            return entity.id

    async def approve(self, payload: OjContestRegistrationIdsRequest, reviewer_id: str) -> None:
        async with transactional(self.db):
            rows = await self._rows_for_ids(payload.contest_id, payload.ids)
            now = utcnow()
            for row in rows:
                row.status = ContestRegistrationStatus.APPROVED
                row.reviewed_at = now
                row.reviewed_by = reviewer_id
                if payload.remark:
                    row.remark = payload.remark
            await self.db.flush()

    async def reject(self, payload: OjContestRegistrationRejectRequest, reviewer_id: str) -> None:
        async with transactional(self.db):
            rows = await self._rows_for_ids(payload.contest_id, payload.ids)
            now = utcnow()
            for row in rows:
                row.status = ContestRegistrationStatus.REJECTED
                row.reviewed_at = now
                row.reviewed_by = reviewer_id
                row.remark = payload.remark
            await self.db.flush()

    async def cancel(self, payload: OjContestRegistrationIdsRequest) -> None:
        async with transactional(self.db):
            rows = await self._rows_for_ids(payload.contest_id, payload.ids)
            for row in rows:
                row.status = ContestRegistrationStatus.CANCELLED
            await self.db.flush()

    async def _rows_for_ids(self, contest_id: str, ids: list[str]) -> list[OjContestRegistration]:
        rows = list(
            (
                await self.db.execute(
                    select(OjContestRegistration).where(
                        OjContestRegistration.contest_id == contest_id,
                        OjContestRegistration.id.in_(ids),
                    )
                )
            )
            .scalars()
            .all()
        )
        if len(rows) != len(set(ids)):
            raise NotFoundError("报名记录不存在")
        return rows

    def _in_register_window(self, contest: OjContest, now=None) -> bool:
        now = now or utcnow()
        if contest.register_start and now < contest.register_start:
            return False
        if contest.register_end and now > contest.register_end:
            return False
        # If no window configured for public contest, allow from visibility until start? Spec says configurable.
        # If both null: allow until contest ends for late register, or require window.
        # Spec: register_start/end for public; if null treat as open from now until start_time for safety.
        if contest.register_start is None and contest.register_end is None:
            return now <= contest.end_time
        return True

    async def register(
        self,
        *,
        contest_id: str,
        account_id: str,
        access_code: str | None = None,
    ) -> OjContestRegistration:
        contest = await self.db.get(OjContest, contest_id)
        if contest is None or not contest.is_visible:
            raise NotFoundError("竞赛不存在")
        if contest.is_private:
            raise BusinessError("私有竞赛不可自助报名，请联系管理员添加")
        if lifecycle_status(contest) == ContestLifecycleStatus.LOCKED:
            raise BusinessError("竞赛已锁定")
        if not self._in_register_window(contest):
            raise BusinessError("当前不在报名时间内")
        if contest.access_code and (access_code or "").strip() != contest.access_code:
            raise BusinessError("准入码错误")

        banned = (
            await self.db.execute(
                select(OjContestBannedUser.id).where(
                    OjContestBannedUser.contest_id == contest_id,
                    OjContestBannedUser.account_id == account_id,
                )
            )
        ).scalar_one_or_none()
        if banned:
            raise BusinessError("你已被禁止参加本场竞赛")

        async with transactional(self.db):
            existing = await self.get_by_user(contest_id, account_id)
            now = utcnow()
            mode = contest.registration_mode or ContestRegistrationMode.AUTO
            target = (
                ContestRegistrationStatus.APPROVED
                if mode == ContestRegistrationMode.AUTO
                else ContestRegistrationStatus.PENDING
            )
            if existing:
                if existing.status in (ContestRegistrationStatus.PENDING, ContestRegistrationStatus.APPROVED):
                    return existing
                # REJECTED / CANCELLED → re-apply
                existing.status = target
                existing.source = ContestRegistrationSource.SELF
                existing.applied_at = now
                existing.remark = None
                if target == ContestRegistrationStatus.APPROVED:
                    existing.reviewed_at = now
                    existing.reviewed_by = None
                else:
                    existing.reviewed_at = None
                    existing.reviewed_by = None
                await self.db.flush()
                return existing

            entity = OjContestRegistration(
                id=generate_snowflake_id(),
                contest_id=contest_id,
                account_id=account_id,
                status=target,
                source=ContestRegistrationSource.SELF,
                applied_at=now,
                reviewed_at=now if target == ContestRegistrationStatus.APPROVED else None,
                reviewed_by=None,
            )
            self.db.add(entity)
            await self.db.flush()
            return entity

    async def unregister(self, *, contest_id: str, account_id: str) -> None:
        contest = await self.db.get(OjContest, contest_id)
        if contest is None:
            raise NotFoundError("竞赛不存在")
        part = (
            await self.db.execute(
                select(OjContestParticipation.id).where(
                    OjContestParticipation.contest_id == contest_id,
                    OjContestParticipation.account_id == account_id,
                    OjContestParticipation.virtual == ContestParticipationVirtual.LIVE,
                )
            )
        ).scalar_one_or_none()
        if part:
            raise BusinessError("已进入比赛，无法取消报名")

        async with transactional(self.db):
            row = await self.get_by_user(contest_id, account_id)
            if row is None:
                raise BusinessError("尚未报名")
            if row.status not in (ContestRegistrationStatus.PENDING, ContestRegistrationStatus.APPROVED):
                raise BusinessError("当前状态无法取消报名")
            row.status = ContestRegistrationStatus.CANCELLED
            await self.db.flush()

    async def enter(self, *, contest_id: str, account_id: str) -> tuple[OjContestParticipation, str | None]:
        """Activate LIVE participation. Returns (participation, first_problem_id)."""
        contest = await self.db.get(OjContest, contest_id)
        if contest is None or not contest.is_visible:
            raise NotFoundError("竞赛不存在")
        status = lifecycle_status(contest)
        if status == ContestLifecycleStatus.LOCKED:
            raise BusinessError("竞赛已锁定")
        if status == ContestLifecycleStatus.SCHEDULED:
            raise BusinessError("竞赛尚未开始")
        if status == ContestLifecycleStatus.ENDED:
            raise BusinessError("竞赛已结束")

        banned = (
            await self.db.execute(
                select(OjContestBannedUser.id).where(
                    OjContestBannedUser.contest_id == contest_id,
                    OjContestBannedUser.account_id == account_id,
                )
            )
        ).scalar_one_or_none()
        if banned:
            raise BusinessError("你已被禁止参加本场竞赛")

        await self.ensure_approved(contest_id, account_id)

        async with transactional(self.db):
            existing = (
                await self.db.execute(
                    select(OjContestParticipation).where(
                        OjContestParticipation.contest_id == contest_id,
                        OjContestParticipation.account_id == account_id,
                        OjContestParticipation.virtual == ContestParticipationVirtual.LIVE,
                    )
                )
            ).scalar_one_or_none()
            if existing:
                if existing.is_disqualified:
                    raise BusinessError("已被取消资格")
                part = existing
            else:
                part = OjContestParticipation(
                    id=generate_snowflake_id(),
                    contest_id=contest_id,
                    account_id=account_id,
                    real_start=utcnow(),
                    score=0,
                    cumtime=0,
                    tiebreaker=0,
                    is_disqualified=False,
                    virtual=int(ContestParticipationVirtual.LIVE),
                    rate_exclude=False,
                    format_data={},
                )
                self.db.add(part)
                contest.user_count = int(contest.user_count or 0) + 1
                await self.db.flush()

        first = (
            await self.db.execute(
                select(OjContestProblem.problem_id)
                .where(OjContestProblem.contest_id == contest_id)
                .order_by(OjContestProblem.sort.asc())
                .limit(1)
            )
        ).scalar_one_or_none()
        return part, first

    @staticmethod
    def can_register(contest: OjContest, reg: OjContestRegistration | None, now=None) -> bool:
        if contest.is_private:
            return False
        if not contest.is_visible:
            return False
        svc_now = now or utcnow()
        if contest.register_start and svc_now < contest.register_start:
            return False
        if contest.register_end and svc_now > contest.register_end:
            return False
        if contest.register_start is None and contest.register_end is None:
            if svc_now > contest.end_time:
                return False
        if reg and reg.status in (ContestRegistrationStatus.PENDING, ContestRegistrationStatus.APPROVED):
            return False
        return True

    @staticmethod
    def can_enter(contest: OjContest, reg: OjContestRegistration | None, entered: bool) -> bool:
        if lifecycle_status(contest) != ContestLifecycleStatus.RUNNING:
            return False
        if reg is None or reg.status != ContestRegistrationStatus.APPROVED:
            return False
        return True  # entered users can still "enter" (idempotent)

    async def list_mine_contest_ids(self, account_id: str) -> list[str]:
        rows = (
            await self.db.execute(
                select(OjContestRegistration.contest_id).where(
                    OjContestRegistration.account_id == account_id,
                    OjContestRegistration.status.in_(
                        [
                            ContestRegistrationStatus.PENDING,
                            ContestRegistrationStatus.APPROVED,
                        ]
                    ),
                )
            )
        ).scalars().all()
        return list(rows)

    async def public_list_filter(self):
        return and_(
            OjContest.is_visible.is_(True),
            or_(
                OjContest.list_visibility == ContestListVisibility.PUBLIC,
                OjContest.list_visibility.is_(None),
            ),
        )
