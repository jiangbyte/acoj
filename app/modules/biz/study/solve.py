"""Shared helpers for study features (solve status from submissions)."""

from __future__ import annotations

from datetime import date, datetime, time, timedelta
from zoneinfo import ZoneInfo

from sqlalchemy import Select, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.modules.biz.submission.enums import SubmissionKind, SubmissionResult
from app.modules.biz.submission.submission.model import OjSubmission

SHANGHAI = ZoneInfo("Asia/Shanghai")


def shanghai_today() -> date:
    return datetime.now(SHANGHAI).date()


def shanghai_day_bounds(day: date) -> tuple[datetime, datetime]:
    start = datetime.combine(day, time.min, tzinfo=SHANGHAI)
    end = start + timedelta(days=1)
    return start, end


async def solved_problem_ids(
    db: AsyncSession,
    account_id: str,
    problem_ids: list[str] | None = None,
) -> set[str]:
    if not account_id:
        return set()
    filters = [
        OjSubmission.user_id == account_id,
        OjSubmission.result == SubmissionResult.AC.value,
        OjSubmission.kind != SubmissionKind.TRIAL.value,
    ]
    if problem_ids is not None:
        if not problem_ids:
            return set()
        filters.append(OjSubmission.problem_id.in_(problem_ids))
    rows = (await db.execute(select(OjSubmission.problem_id).where(*filters).distinct())).scalars().all()
    return set(rows)


async def attempted_problem_ids(
    db: AsyncSession,
    account_id: str,
    problem_ids: list[str] | None = None,
) -> set[str]:
    if not account_id:
        return set()
    filters = [
        OjSubmission.user_id == account_id,
        OjSubmission.kind != SubmissionKind.TRIAL.value,
        OjSubmission.result.is_not(None),
    ]
    if problem_ids is not None:
        if not problem_ids:
            return set()
        filters.append(OjSubmission.problem_id.in_(problem_ids))
    rows = (await db.execute(select(OjSubmission.problem_id).where(*filters).distinct())).scalars().all()
    return set(rows)


async def user_has_ac_on_problem(db: AsyncSession, account_id: str, problem_id: str) -> bool:
    ids = await solved_problem_ids(db, account_id, [problem_id])
    return problem_id in ids


def non_trial_ac_stmt(account_id: str) -> Select:
    return select(OjSubmission).where(
        OjSubmission.user_id == account_id,
        OjSubmission.result == SubmissionResult.AC.value,
        OjSubmission.kind != SubmissionKind.TRIAL.value,
    )


async def count_submissions(db: AsyncSession, account_id: str) -> tuple[int, int]:
    """Return (total_non_trial, ac_count)."""
    base = [
        OjSubmission.user_id == account_id,
        OjSubmission.kind != SubmissionKind.TRIAL.value,
        OjSubmission.result.is_not(None),
    ]
    total = int(
        (await db.execute(select(func.count()).select_from(OjSubmission).where(*base))).scalar_one() or 0
    )
    ac = int(
        (
            await db.execute(
                select(func.count())
                .select_from(OjSubmission)
                .where(*base, OjSubmission.result == SubmissionResult.AC.value)
            )
        ).scalar_one()
        or 0
    )
    return total, ac
