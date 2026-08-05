"""User solve stats for portal profile."""

from __future__ import annotations

from collections import defaultdict
from datetime import date, datetime

from pydantic import Field
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import AuthenticationError
from app.core.response.pagination import PageData, PageQuery, build_page
from app.core.schema.base import ApiSchema
from app.core.security.session import SessionPayload
from app.modules.biz.problem.enums import ProblemDifficulty, ProblemStatus
from app.modules.biz.problem.problem.model import OjProblem
from app.modules.biz.study.daily.service import DailyService, DailyTodayQuery
from app.modules.biz.study.solve import SHANGHAI, count_submissions, solved_problem_ids
from app.modules.biz.submission.enums import SubmissionKind, SubmissionResult
from app.modules.biz.submission.submission.model import OjSubmission


class DifficultyStat(ApiSchema):
    difficulty: str
    solved: int = 0
    total: int = 0


class UserStatsSchema(ApiSchema):
    account_id: str
    solved_total: int = 0
    problem_total: int = 0
    submission_total: int = 0
    ac_submission_total: int = 0
    ac_rate: float = 0
    streak: int = 0
    by_difficulty: list[DifficultyStat] = Field(default_factory=list)


class HeatmapDay(ApiSchema):
    day_date: date
    count: int = 0


class UserHeatmapSchema(ApiSchema):
    year: int
    days: list[HeatmapDay] = Field(default_factory=list)
    total_submissions: int = 0
    active_days: int = 0


class RecentSolvedItem(ApiSchema):
    problem_id: str
    problem_code: str
    problem_name: str
    difficulty: str
    solved_at: datetime


class UserStatsQuery(ApiSchema):
    account_id: str | None = None


class UserHeatmapQuery(ApiSchema):
    year: int
    account_id: str | None = None


class UserRecentSolvedQuery(PageQuery):
    account_id: str | None = None


class UserStatsService:
    def __init__(self, db: AsyncSession):
        self.db = db

    def _resolve_account_id(
        self,
        query: UserStatsQuery | UserHeatmapQuery | UserRecentSolvedQuery,
        session: SessionPayload | None,
    ) -> str:
        account_id = (session.account_id if session else None) or query.account_id
        if not account_id:
            raise AuthenticationError("需要登录或指定 account_id")
        return account_id

    async def stats(
        self,
        query: UserStatsQuery,
        *,
        session: SessionPayload | None = None,
    ) -> UserStatsSchema:
        account_id = self._resolve_account_id(query, session)
        public_filters = [
            OjProblem.status == ProblemStatus.PUBLISHED.value,
            OjProblem.is_public.is_(True),
        ]
        totals = dict(
            (
                await self.db.execute(
                    select(OjProblem.difficulty, func.count())
                    .where(*public_filters)
                    .group_by(OjProblem.difficulty)
                )
            ).all()
        )
        solved_ids = await solved_problem_ids(self.db, account_id)
        solved_rows = []
        if solved_ids:
            solved_rows = list(
                (
                    await self.db.execute(
                        select(OjProblem.difficulty, func.count())
                        .where(*public_filters, OjProblem.id.in_(solved_ids))
                        .group_by(OjProblem.difficulty)
                    )
                ).all()
            )
        solved_map = {d or ProblemDifficulty.MEDIUM.value: int(c) for d, c in solved_rows}
        by_diff: list[DifficultyStat] = []
        problem_total = 0
        solved_total = 0
        for diff in (ProblemDifficulty.EASY.value, ProblemDifficulty.MEDIUM.value, ProblemDifficulty.HARD.value):
            total = int(totals.get(diff, 0))
            # also count null/other into medium
            if diff == ProblemDifficulty.MEDIUM.value:
                for k, v in totals.items():
                    if k not in (
                        ProblemDifficulty.EASY.value,
                        ProblemDifficulty.MEDIUM.value,
                        ProblemDifficulty.HARD.value,
                    ):
                        total += int(v)
            solved = int(solved_map.get(diff, 0))
            if diff == ProblemDifficulty.MEDIUM.value:
                for k, v in solved_map.items():
                    if k not in (
                        ProblemDifficulty.EASY.value,
                        ProblemDifficulty.MEDIUM.value,
                        ProblemDifficulty.HARD.value,
                    ):
                        solved += int(v)
            problem_total += total
            solved_total += solved
            by_diff.append(DifficultyStat(difficulty=diff, solved=solved, total=total))

        sub_total, ac_total = await count_submissions(self.db, account_id)
        ac_rate = round(ac_total / sub_total * 100, 1) if sub_total else 0.0
        streak = (await DailyService(self.db).today(DailyTodayQuery(account_id=account_id))).streak
        return UserStatsSchema(
            account_id=account_id,
            solved_total=solved_total,
            problem_total=problem_total,
            submission_total=sub_total,
            ac_submission_total=ac_total,
            ac_rate=ac_rate,
            streak=streak,
            by_difficulty=by_diff,
        )

    async def heatmap(
        self,
        query: UserHeatmapQuery,
        *,
        session: SessionPayload | None = None,
    ) -> UserHeatmapSchema:
        account_id = self._resolve_account_id(query, session)
        year = query.year
        start = datetime(year, 1, 1, tzinfo=SHANGHAI)
        end = datetime(year + 1, 1, 1, tzinfo=SHANGHAI)
        rows = (
            await self.db.execute(
                select(OjSubmission.created_at).where(
                    OjSubmission.user_id == account_id,
                    OjSubmission.kind != SubmissionKind.TRIAL.value,
                    OjSubmission.created_at >= start,
                    OjSubmission.created_at < end,
                )
            )
        ).scalars().all()
        counter: dict[date, int] = defaultdict(int)
        for created_at in rows:
            if created_at is None:
                continue
            local = created_at.astimezone(SHANGHAI).date() if created_at.tzinfo else created_at.date()
            counter[local] += 1
        days = [HeatmapDay(day_date=d, count=c) for d, c in sorted(counter.items())]
        return UserHeatmapSchema(
            year=year,
            days=days,
            total_submissions=sum(counter.values()),
            active_days=len(counter),
        )

    async def recent_solved(
        self,
        query: UserRecentSolvedQuery,
        *,
        session: SessionPayload | None = None,
    ) -> PageData[RecentSolvedItem]:
        account_id = self._resolve_account_id(query, session)
        # Distinct problems with latest AC time
        subq = (
            select(
                OjSubmission.problem_id.label("problem_id"),
                func.max(OjSubmission.created_at).label("solved_at"),
            )
            .where(
                OjSubmission.user_id == account_id,
                OjSubmission.result == SubmissionResult.AC.value,
                OjSubmission.kind != SubmissionKind.TRIAL.value,
            )
            .group_by(OjSubmission.problem_id)
            .subquery()
        )
        total = int((await self.db.execute(select(func.count()).select_from(subq))).scalar_one() or 0)
        rows = (
            await self.db.execute(
                select(subq.c.problem_id, subq.c.solved_at, OjProblem.code, OjProblem.name, OjProblem.difficulty)
                .join(OjProblem, OjProblem.id == subq.c.problem_id)
                .order_by(subq.c.solved_at.desc())
                .offset(query.pagination.offset)
                .limit(query.pagination.size)
            )
        ).all()
        items = [
            RecentSolvedItem(
                problem_id=pid,
                problem_code=code,
                problem_name=name,
                difficulty=diff or ProblemDifficulty.MEDIUM.value,
                solved_at=solved_at,
            )
            for pid, solved_at, code, name, diff in rows
        ]
        return build_page(query.pagination, total, items)
