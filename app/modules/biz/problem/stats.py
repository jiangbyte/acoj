"""Problem pass-rate / solver-count refresh from submissions."""

from __future__ import annotations

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.modules.biz.problem.problem.model import OjProblem
from app.modules.biz.submission.enums import SubmissionKind, SubmissionResult
from app.modules.biz.submission.submission.model import OjSubmission


async def refresh_problem_ac_stats(db: AsyncSession, problem_id: str) -> None:
    """Recompute oj_problem.user_count / ac_rate from non-trial judged submissions."""
    base = [
        OjSubmission.problem_id == problem_id,
        OjSubmission.kind != SubmissionKind.TRIAL.value,
        OjSubmission.result.is_not(None),
    ]
    total = int(
        (await db.execute(select(func.count()).select_from(OjSubmission).where(*base))).scalar_one()
        or 0
    )
    ac_cnt = int(
        (
            await db.execute(
                select(func.count())
                .select_from(OjSubmission)
                .where(*base, OjSubmission.result == SubmissionResult.AC.value)
            )
        ).scalar_one()
        or 0
    )
    user_cnt = int(
        (
            await db.execute(
                select(func.count(func.distinct(OjSubmission.user_id))).where(
                    *base,
                    OjSubmission.result == SubmissionResult.AC.value,
                )
            )
        ).scalar_one()
        or 0
    )
    problem = await db.get(OjProblem, problem_id)
    if problem is None:
        return
    problem.user_count = user_cnt
    problem.ac_rate = round(ac_cnt / total * 100.0, 2) if total else 0.0
    await db.flush()
