"""Contest scoring: convert submission points + recompute participation + scoreboard."""

from __future__ import annotations

from collections import defaultdict
from typing import Any

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.modules.biz.contest.contest.model import OjContest
from app.modules.biz.contest.enums import ContestParticipationVirtual
from app.modules.biz.contest.formats.base import SubmissionScoreInput
from app.modules.biz.contest.formats.registry import get_format
from app.modules.biz.contest.lifecycle import contest_is_frozen, ensure_aware, lifecycle_status
from app.modules.biz.contest.participation.model import OjContestParticipation
from app.modules.biz.contest.problem.model import OjContestProblem
from app.modules.biz.problem.test_case.model import OjProblemTestCase
from app.modules.biz.submission.submission.model import OjContestSubmission, OjSubmission, OjSubmissionCase
from app.platform.id_generator.snowflake import generate_snowflake_id


def compute_contest_points(
    *,
    case_points: float,
    case_total: float,
    submission_score: float,
    contest_problem_points: float,
    partial: bool,
    points_precision: int,
) -> float:
    full = float(contest_problem_points)
    if full <= 0:
        return 0.0
    if case_total and case_total > 0:
        ratio = float(case_points) / float(case_total)
    else:
        # fallback: submission.score assumed on problem.points scale 0..100 or 0..full
        ratio = min(max(float(submission_score) / 100.0, 0.0), 1.0) if submission_score <= 100 else min(
            float(submission_score) / full, 1.0
        )
    pts = round(ratio * full, max(0, int(points_precision)))
    if not partial and abs(pts - full) > 1e-9:
        return 0.0
    return pts


async def _batch_map_for_problem(db: AsyncSession, problem_id: str) -> dict[int, int | None]:
    """case_no -> batch_no."""
    rows = (
        await db.execute(
            select(OjProblemTestCase.case_no, OjProblemTestCase.batch_no).where(
                OjProblemTestCase.problem_id == problem_id
            )
        )
    ).all()
    return {int(case_no): batch_no for case_no, batch_no in rows}


async def build_submission_inputs(
    db: AsyncSession,
    participation_id: str,
) -> tuple[list[SubmissionScoreInput], list[dict[str, Any]]]:
    part = await db.get(OjContestParticipation, participation_id)
    if part is None:
        return [], []
    problems = list(
        (
            await db.execute(
                select(OjContestProblem)
                .where(OjContestProblem.contest_id == part.contest_id)
                .order_by(OjContestProblem.sort.asc())
            )
        ).scalars().all()
    )
    problem_dicts = [
        {
            "id": p.id,
            "problem_id": p.problem_id,
            "label": p.label or "",
            "points": float(p.points or 0),
            "partial": bool(p.partial),
        }
        for p in problems
    ]

    rows = (
        await db.execute(
            select(OjContestSubmission, OjSubmission)
            .join(OjSubmission, OjSubmission.id == OjContestSubmission.submission_id)
            .where(OjContestSubmission.participation_id == participation_id)
            .options(selectinload(OjSubmission.cases))
            .order_by(OjSubmission.created_at.asc())
        )
    ).all()

    batch_cache: dict[str, dict[int, int | None]] = {}
    inputs: list[SubmissionScoreInput] = []
    for cs, sub in rows:
        if sub.problem_id not in batch_cache:
            batch_cache[sub.problem_id] = await _batch_map_for_problem(db, sub.problem_id)
        case_map = batch_cache[sub.problem_id]
        case_scores = [(c.case_no, float(c.score or 0), c.result) for c in (sub.cases or [])]
        # batch points: sum scores per batch for this submission
        batch_acc: dict[str, float] = defaultdict(float)
        batch_full: dict[str, list[tuple[float, str | None]]] = defaultdict(list)
        for case_no, score, result in case_scores:
            bn = case_map.get(case_no)
            key = str(bn if bn is not None else 0)
            batch_acc[key] += score
            batch_full[key].append((score, result))
        # For IOI: if batch has any non-AC and we want min-batch semantics like DMOJ,
        # use sum within batch (our worker already zeros failed batches in aggregate).
        # Here we keep summed case scores per batch for this submission.
        inputs.append(
            SubmissionScoreInput(
                contest_problem_id=cs.contest_problem_id,
                problem_id=sub.problem_id,
                points=float(cs.points or 0),
                result=sub.result,
                created_at=sub.created_at,
                is_pretest=bool(cs.is_pretest),
                case_scores=case_scores,
                batch_points=dict(batch_acc) if batch_acc else {"0": float(cs.points or 0)},
            )
        )
    return inputs, problem_dicts


async def recompute_participation(db: AsyncSession, participation_id: str) -> OjContestParticipation:
    part = await db.get(OjContestParticipation, participation_id)
    if part is None:
        raise ValueError("participation not found")
    contest = await db.get(OjContest, part.contest_id)
    if contest is None:
        raise ValueError("contest not found")
    if part.is_disqualified:
        part.score = -9999.0
        part.cumtime = 0
        part.tiebreaker = 0
        part.format_data = {}
        await db.flush()
        return part

    submissions, problems = await build_submission_inputs(db, participation_id)
    fmt = get_format(contest)
    score, cumtime, tiebreaker, format_data = fmt.update_participation(
        real_start=part.real_start,
        submissions=submissions,
        problems=problems,
    )
    part.score = float(score)
    part.cumtime = int(cumtime)
    part.tiebreaker = float(tiebreaker)
    part.format_data = format_data
    await db.flush()
    return part


async def apply_contest_submission_result(db: AsyncSession, submission_id: str) -> None:
    """Hot path after judge: update contest_submission.points and recompute participation."""
    stmt = (
        select(OjContestSubmission, OjSubmission, OjContestProblem, OjContest)
        .join(OjSubmission, OjSubmission.id == OjContestSubmission.submission_id)
        .join(OjContestProblem, OjContestProblem.id == OjContestSubmission.contest_problem_id)
        .join(OjContest, OjContest.id == OjContestProblem.contest_id)
        .where(OjContestSubmission.submission_id == submission_id)
    )
    row = (await db.execute(stmt)).first()
    if row is None:
        return
    cs, sub, cproblem, contest = row
    cs.points = compute_contest_points(
        case_points=float(sub.case_points or 0),
        case_total=float(sub.case_total or 0),
        submission_score=float(sub.score or 0),
        contest_problem_points=float(cproblem.points or 0),
        partial=bool(cproblem.partial),
        points_precision=int(contest.points_precision or 0),
    )
    await db.flush()
    await recompute_participation(db, cs.participation_id)


async def rescore_contest(db: AsyncSession, contest_id: str) -> int:
    parts = list(
        (
            await db.execute(
                select(OjContestParticipation).where(OjContestParticipation.contest_id == contest_id)
            )
        ).scalars().all()
    )
    # refresh all contest submission points first
    contest = await db.get(OjContest, contest_id)
    if contest is None:
        return 0
    cproblems = {
        p.id: p
        for p in (
            await db.execute(select(OjContestProblem).where(OjContestProblem.contest_id == contest_id))
        ).scalars().all()
    }
    rows = (
        await db.execute(
            select(OjContestSubmission, OjSubmission)
            .join(OjSubmission, OjSubmission.id == OjContestSubmission.submission_id)
            .where(OjContestSubmission.contest_problem_id.in_(list(cproblems.keys()) or ["__none__"]))
        )
    ).all()
    for cs, sub in rows:
        cp = cproblems.get(cs.contest_problem_id)
        if not cp:
            continue
        cs.points = compute_contest_points(
            case_points=float(sub.case_points or 0),
            case_total=float(sub.case_total or 0),
            submission_score=float(sub.score or 0),
            contest_problem_points=float(cp.points or 0),
            partial=bool(cp.partial),
            points_precision=int(contest.points_precision or 0),
        )
    await db.flush()
    for part in parts:
        await recompute_participation(db, part.id)
    return len(parts)


async def build_scoreboard(
    db: AsyncSession,
    contest_id: str,
    *,
    virtual: int = ContestParticipationVirtual.LIVE,
    ignore_freeze: bool = True,
) -> dict[str, Any]:
    contest = await db.get(OjContest, contest_id)
    if contest is None:
        raise ValueError("contest not found")
    problems = list(
        (
            await db.execute(
                select(OjContestProblem)
                .where(OjContestProblem.contest_id == contest_id)
                .order_by(OjContestProblem.sort.asc())
            )
        ).scalars().all()
    )
    parts = list(
        (
            await db.execute(
                select(OjContestParticipation).where(
                    OjContestParticipation.contest_id == contest_id,
                    OjContestParticipation.virtual == virtual,
                )
            )
        ).scalars().all()
    )
    fmt = get_format(contest)
    rows = []
    for part in parts:
        rows.append(
            {
                "participation_id": part.id,
                "account_id": part.account_id,
                "score": part.score,
                "cumtime": part.cumtime,
                "tiebreaker": part.tiebreaker,
                "is_disqualified": part.is_disqualified,
                "format_data": part.format_data or {},
                "real_start": part.real_start.isoformat() if part.real_start else None,
            }
        )
    rows.sort(key=fmt.sort_key)
    frozen = contest_is_frozen(contest) and not ignore_freeze
    # freeze: hide late submissions in format_data for non-admin — simplified: clear cells updated after freeze
    if frozen:
        from datetime import timedelta

        freeze_at = ensure_aware(contest.end_time) - timedelta(seconds=int(contest.freeze_seconds or 0))
        for row in rows:
            # mark frozen without stripping admin view; portal passes ignore_freeze=False
            row["frozen"] = True
            row["freeze_at"] = freeze_at.isoformat()

    for i, row in enumerate(rows, start=1):
        row["rank"] = i

    return {
        "contest_id": contest_id,
        "format_name": contest.format_name,
        "lifecycle_status": lifecycle_status(contest).value,
        "is_frozen": contest_is_frozen(contest),
        "problems": [
            {
                "id": p.id,
                "problem_id": p.problem_id,
                "label": p.label,
                "points": p.points,
                "partial": p.partial,
                "sort": p.sort,
            }
            for p in problems
        ],
        "rows": rows,
    }
