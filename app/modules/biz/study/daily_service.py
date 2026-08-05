"""Daily problem schemas + service."""

from __future__ import annotations

from datetime import date, datetime, timedelta
from math import ceil

from pydantic import Field
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import BusinessError, NotFoundError
from app.core.response.pagination import PageData, PageQuery, build_page
from app.core.schema.base import ApiSchema, IdsRequest
from app.modules.biz.problem.enums import ProblemDifficulty
from app.modules.biz.problem.problem.model import OjProblem
from app.modules.biz.study.model import OjDailyProblem
from app.modules.biz.study.solve import SHANGHAI, shanghai_today, user_has_ac_on_problem
from app.modules.biz.submission.enums import SubmissionKind, SubmissionResult
from app.modules.biz.submission.submission.model import OjSubmission
from app.platform.db.transaction import transactional
from app.platform.id_generator.snowflake import generate_snowflake_id


class DailyUpsertRequest(ApiSchema):
    day_date: date
    problem_id: str


class DailyAdminPageQuery(ApiSchema):
    pagination: PageQuery
    from_date: date | None = None
    to_date: date | None = None


class DailyProblemBrief(ApiSchema):
    id: str
    day_date: date
    problem_id: str
    problem_code: str | None = None
    problem_name: str | None = None
    difficulty: str | None = None
    ac_rate: float = 0
    checked_in: bool = False


class DailyTodaySchema(ApiSchema):
    day_date: date
    problem: DailyProblemBrief | None = None
    checked_in: bool = False
    streak: int = 0
    month_done: int = 0
    month_total: int = 0


class DailyCalendarDay(ApiSchema):
    day_date: date
    has_problem: bool = False
    checked_in: bool = False
    problem_id: str | None = None


class DailyCalendarSchema(ApiSchema):
    year: int
    month: int
    days: list[DailyCalendarDay] = Field(default_factory=list)
    streak: int = 0
    month_done: int = 0
    month_total: int = 0


class DailyService:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def _problem_map(self, problem_ids: list[str]) -> dict[str, OjProblem]:
        if not problem_ids:
            return {}
        rows = list((await self.db.execute(select(OjProblem).where(OjProblem.id.in_(problem_ids)))).scalars().all())
        return {r.id: r for r in rows}

    async def upsert(self, payload: DailyUpsertRequest) -> str:
        async with transactional(self.db):
            problem = await self.db.get(OjProblem, payload.problem_id)
            if problem is None:
                raise NotFoundError("题目不存在")
            row = (
                await self.db.execute(select(OjDailyProblem).where(OjDailyProblem.day_date == payload.day_date))
            ).scalar_one_or_none()
            if row is None:
                row = OjDailyProblem(id=generate_snowflake_id(), day_date=payload.day_date, problem_id=payload.problem_id)
                self.db.add(row)
            else:
                row.problem_id = payload.problem_id
            await self.db.flush()
            return row.id

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            for did in payload.ids:
                row = await self.db.get(OjDailyProblem, did)
                if row:
                    await self.db.delete(row)
            await self.db.flush()

    async def page_admin(self, query: DailyAdminPageQuery) -> PageData[DailyProblemBrief]:
        filters = []
        if query.from_date:
            filters.append(OjDailyProblem.day_date >= query.from_date)
        if query.to_date:
            filters.append(OjDailyProblem.day_date <= query.to_date)
        stmt = select(OjDailyProblem)
        count_stmt = select(func.count()).select_from(OjDailyProblem)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        total = int((await self.db.execute(count_stmt)).scalar_one() or 0)
        rows = list(
            (
                await self.db.execute(
                    stmt.order_by(OjDailyProblem.day_date.desc())
                    .offset(query.pagination.offset)
                    .limit(query.pagination.size)
                )
            )
            .scalars()
            .all()
        )
        pmap = await self._problem_map([r.problem_id for r in rows])
        items = [
            DailyProblemBrief(
                id=r.id,
                day_date=r.day_date,
                problem_id=r.problem_id,
                problem_code=(pmap.get(r.problem_id).code if pmap.get(r.problem_id) else None),
                problem_name=(pmap.get(r.problem_id).name if pmap.get(r.problem_id) else None),
                difficulty=(pmap.get(r.problem_id).difficulty if pmap.get(r.problem_id) else None),
                ac_rate=float(pmap[r.problem_id].ac_rate) if pmap.get(r.problem_id) else 0,
            )
            for r in rows
        ]
        return build_page(query.pagination, total, items)

    async def _checked_in_dates(self, account_id: str, days: list[OjDailyProblem]) -> set[date]:
        if not account_id or not days:
            return set()
        # User AC'd the configured problem (any time) counts as check-in for that day.
        result: set[date] = set()
        for d in days:
            if await user_has_ac_on_problem(self.db, account_id, d.problem_id):
                result.add(d.day_date)
        return result

    async def _streak(self, account_id: str) -> int:
        if not account_id:
            return 0
        today = shanghai_today()
        # Look back up to 90 configured days
        rows = list(
            (
                await self.db.execute(
                    select(OjDailyProblem)
                    .where(OjDailyProblem.day_date <= today)
                    .order_by(OjDailyProblem.day_date.desc())
                    .limit(90)
                )
            )
            .scalars()
            .all()
        )
        checked = await self._checked_in_dates(account_id, rows)
        streak = 0
        cursor = today
        by_date = {r.day_date: r for r in rows}
        # If today has no problem or not checked, allow streak to start from yesterday
        if today not in checked:
            cursor = today - timedelta(days=1)
        while True:
            if cursor not in by_date:
                break
            if cursor not in checked:
                break
            streak += 1
            cursor -= timedelta(days=1)
        return streak

    async def today(self, account_id: str | None) -> DailyTodaySchema:
        today = shanghai_today()
        row = (
            await self.db.execute(select(OjDailyProblem).where(OjDailyProblem.day_date == today))
        ).scalar_one_or_none()
        month_start = today.replace(day=1)
        if today.month == 12:
            month_end = today.replace(year=today.year + 1, month=1, day=1) - timedelta(days=1)
        else:
            month_end = today.replace(month=today.month + 1, day=1) - timedelta(days=1)
        month_rows = list(
            (
                await self.db.execute(
                    select(OjDailyProblem).where(
                        OjDailyProblem.day_date >= month_start,
                        OjDailyProblem.day_date <= month_end,
                    )
                )
            )
            .scalars()
            .all()
        )
        checked = await self._checked_in_dates(account_id or "", month_rows) if account_id else set()
        problem_brief = None
        checked_in = False
        if row:
            pmap = await self._problem_map([row.problem_id])
            p = pmap.get(row.problem_id)
            checked_in = today in checked
            problem_brief = DailyProblemBrief(
                id=row.id,
                day_date=row.day_date,
                problem_id=row.problem_id,
                problem_code=p.code if p else None,
                problem_name=p.name if p else None,
                difficulty=p.difficulty if p else ProblemDifficulty.MEDIUM.value,
                ac_rate=float(p.ac_rate) if p else 0,
                checked_in=checked_in,
            )
        return DailyTodaySchema(
            day_date=today,
            problem=problem_brief,
            checked_in=checked_in,
            streak=await self._streak(account_id) if account_id else 0,
            month_done=len(checked),
            month_total=len(month_rows),
        )

    async def calendar(self, year: int, month: int, account_id: str | None) -> DailyCalendarSchema:
        if month < 1 or month > 12:
            raise BusinessError("无效月份")
        start = date(year, month, 1)
        if month == 12:
            end = date(year + 1, 1, 1) - timedelta(days=1)
        else:
            end = date(year, month + 1, 1) - timedelta(days=1)
        rows = list(
            (
                await self.db.execute(
                    select(OjDailyProblem).where(
                        OjDailyProblem.day_date >= start,
                        OjDailyProblem.day_date <= end,
                    )
                )
            )
            .scalars()
            .all()
        )
        by_date = {r.day_date: r for r in rows}
        checked = await self._checked_in_dates(account_id or "", rows) if account_id else set()
        days: list[DailyCalendarDay] = []
        cursor = start
        while cursor <= end:
            row = by_date.get(cursor)
            days.append(
                DailyCalendarDay(
                    day_date=cursor,
                    has_problem=row is not None,
                    checked_in=cursor in checked,
                    problem_id=row.problem_id if row else None,
                )
            )
            cursor += timedelta(days=1)
        return DailyCalendarSchema(
            year=year,
            month=month,
            days=days,
            streak=await self._streak(account_id) if account_id else 0,
            month_done=len(checked),
            month_total=len(rows),
        )
