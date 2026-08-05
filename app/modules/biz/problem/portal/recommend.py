"""Portal 题目推荐：基于做题记录 / 难度梯度 / 标签亲和 / 热度的可解释打分。"""

from __future__ import annotations

import math
from collections import Counter
from dataclasses import dataclass

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.modules.biz.problem.enums import ProblemDifficulty, ProblemStatus
from app.modules.biz.problem.problem.model import OjProblem
from app.modules.biz.problem.type.model import OjProblemTypeRel
from app.modules.biz.study.solve import attempted_problem_ids, solved_problem_ids
from app.modules.biz.submission.enums import SubmissionKind, SubmissionResult
from app.modules.biz.submission.submission.model import OjSubmission

DIFFICULTY_RANK = {
    ProblemDifficulty.EASY.value: 0,
    ProblemDifficulty.MEDIUM.value: 1,
    ProblemDifficulty.HARD.value: 2,
}
CANDIDATE_LIMIT = 400
RECENT_AC_LIMIT = 30


@dataclass(slots=True)
class ScoredProblem:
    problem: OjProblem
    score: float
    reason: str


def _normalize_difficulty(value: str | None) -> str:
    raw = value or ProblemDifficulty.MEDIUM.value
    if raw in DIFFICULTY_RANK:
        return raw
    return ProblemDifficulty.MEDIUM.value


def _target_difficulty(solved_diff_counts: Counter[str]) -> str:
    """按已 AC 难度分布推下一档训练目标。"""
    easy = solved_diff_counts.get(ProblemDifficulty.EASY.value, 0)
    medium = solved_diff_counts.get(ProblemDifficulty.MEDIUM.value, 0)
    hard = solved_diff_counts.get(ProblemDifficulty.HARD.value, 0)
    total = easy + medium + hard
    if total == 0:
        return ProblemDifficulty.EASY.value
    if easy >= 5 and medium < 3:
        return ProblemDifficulty.MEDIUM.value
    if medium >= 5 and hard < 2:
        return ProblemDifficulty.HARD.value
    if hard >= 3:
        return ProblemDifficulty.HARD.value
    if medium >= 2:
        return ProblemDifficulty.MEDIUM.value
    return ProblemDifficulty.EASY.value


def _difficulty_score(problem_diff: str, target: str) -> tuple[float, str | None]:
    pd = DIFFICULTY_RANK.get(problem_diff, 1)
    td = DIFFICULTY_RANK.get(target, 0)
    gap = abs(pd - td)
    if gap == 0:
        label = {0: "入门巩固", 1: "梯度提升", 2: "进阶挑战"}.get(td, "难度匹配")
        return 42.0, label
    if gap == 1:
        return 18.0, "邻近难度"
    return 0.0, None


def _popularity_score(user_count: int, ac_rate: float) -> float:
    # 通过率适中且有一定做题人数 → 更适合推荐
    rate = max(0.0, min(100.0, float(ac_rate or 0)))
    sweet = 1.0 - abs(rate - 55.0) / 55.0  # 峰值约 55%
    sweet = max(0.0, sweet)
    return min(18.0, math.log1p(max(0, user_count)) * 2.4) + sweet * 12.0


def score_problem(
    *,
    problem: OjProblem,
    target_diff: str,
    solved: set[str],
    attempted: set[str],
    type_affinity: Counter[str],
    problem_type_ids: list[str],
    logged_in: bool,
) -> ScoredProblem | None:
    if problem.id in solved:
        return None

    diff = _normalize_difficulty(getattr(problem, "difficulty", None))
    score = 0.0
    reasons: list[str] = []

    d_score, d_reason = _difficulty_score(diff, target_diff)
    score += d_score
    if d_reason:
        reasons.append(d_reason)

    if logged_in and problem.id in attempted:
        score += 36.0
        reasons.append("做过未过")

    overlap = sum(1 for tid in problem_type_ids if type_affinity.get(tid, 0) > 0)
    if overlap:
        score += min(45.0, overlap * 15.0)
        reasons.append("知识点相关")

    pop = _popularity_score(int(problem.user_count or 0), float(problem.ac_rate or 0))
    score += pop
    if pop >= 16 and "热门练习" not in reasons:
        reasons.append("热门练习")

    if not logged_in:
        # 游客：偏入门高通过率
        if diff == ProblemDifficulty.EASY.value:
            score += 20.0
            reasons = ["新手友好"] + [r for r in reasons if r != "入门巩固"]
        elif diff == ProblemDifficulty.MEDIUM.value:
            score += 8.0

    # 稳定微扰，避免同分完全按 id 固化观感
    jitter = (hash(problem.id) % 1000) / 1000.0
    score += jitter

    reason = " · ".join(reasons[:2]) if reasons else "综合推荐"
    return ScoredProblem(problem=problem, score=score, reason=reason)


async def _recent_ac_type_affinity(db: AsyncSession, account_id: str) -> Counter[str]:
    """最近 AC 题目的类型权重。"""
    recent_ids = list(
        (
            await db.execute(
                select(OjSubmission.problem_id)
                .where(
                    OjSubmission.user_id == account_id,
                    OjSubmission.result == SubmissionResult.AC.value,
                    OjSubmission.kind != SubmissionKind.TRIAL.value,
                )
                .order_by(OjSubmission.created_at.desc())
                .limit(RECENT_AC_LIMIT)
            )
        )
        .scalars()
        .all()
    )
    if not recent_ids:
        return Counter()
    rows = (
        await db.execute(
            select(OjProblemTypeRel.type_id, OjProblemTypeRel.problem_id).where(
                OjProblemTypeRel.problem_id.in_(recent_ids)
            )
        )
    ).all()
    # 越近权重越高
    rank = {pid: idx for idx, pid in enumerate(recent_ids)}
    weights: Counter[str] = Counter()
    for type_id, problem_id in rows:
        w = RECENT_AC_LIMIT - rank.get(problem_id, RECENT_AC_LIMIT - 1)
        weights[type_id] += max(1, w)
    return weights


async def _solved_difficulty_counts(db: AsyncSession, solved_ids: set[str]) -> Counter[str]:
    if not solved_ids:
        return Counter()
    rows = (
        await db.execute(select(OjProblem.difficulty).where(OjProblem.id.in_(list(solved_ids))))
    ).scalars().all()
    return Counter(_normalize_difficulty(d) for d in rows)


async def load_candidates(db: AsyncSession) -> list[OjProblem]:
    return list(
        (
            await db.execute(
                select(OjProblem)
                .where(
                    OjProblem.status == ProblemStatus.PUBLISHED.value,
                    OjProblem.is_public.is_(True),
                )
                .order_by(OjProblem.user_count.desc(), OjProblem.code.asc())
                .limit(CANDIDATE_LIMIT)
            )
        )
        .scalars()
        .all()
    )


@dataclass(slots=True)
class RecommendResult:
    items: list[ScoredProblem]
    strategy: str
    target_difficulty: str


async def recommend_problems(
    db: AsyncSession,
    *,
    account_id: str | None,
    size: int = 8,
) -> RecommendResult:
    size = max(1, min(size, 50))
    candidates = await load_candidates(db)
    if not candidates:
        return RecommendResult(items=[], strategy="empty", target_difficulty=ProblemDifficulty.EASY.value)

    candidate_ids = [p.id for p in candidates]
    type_rows = (
        await db.execute(
            select(OjProblemTypeRel.problem_id, OjProblemTypeRel.type_id).where(
                OjProblemTypeRel.problem_id.in_(candidate_ids)
            )
        )
    ).all()
    type_map: dict[str, list[str]] = {}
    for pid, tid in type_rows:
        type_map.setdefault(pid, []).append(tid)

    solved: set[str] = set()
    attempted: set[str] = set()
    affinity: Counter[str] = Counter()
    target = ProblemDifficulty.EASY.value
    logged_in = bool(account_id)

    if account_id:
        solved = await solved_problem_ids(db, account_id)
        attempted = await attempted_problem_ids(db, account_id)
        affinity = await _recent_ac_type_affinity(db, account_id)
        target = _target_difficulty(await _solved_difficulty_counts(db, solved))

    scored: list[ScoredProblem] = []
    for problem in candidates:
        item = score_problem(
            problem=problem,
            target_diff=target,
            solved=solved,
            attempted=attempted,
            type_affinity=affinity,
            problem_type_ids=type_map.get(problem.id, []),
            logged_in=logged_in,
        )
        if item is not None:
            scored.append(item)

    scored.sort(key=lambda x: x.score, reverse=True)

    # 轻度排序多样性：同难度连续过多时后移一条
    diversified: list[ScoredProblem] = []
    deferred: list[ScoredProblem] = []
    last_diff: str | None = None
    streak = 0
    for item in scored:
        diff = _normalize_difficulty(getattr(item.problem, "difficulty", None))
        if last_diff == diff and streak >= 2:
            deferred.append(item)
            continue
        diversified.append(item)
        if last_diff == diff:
            streak += 1
        else:
            last_diff = diff
            streak = 1
        if len(diversified) >= size:
            break
    if len(diversified) < size:
        for item in deferred:
            diversified.append(item)
            if len(diversified) >= size:
                break

    strategy = "personalized" if logged_in else "guest_popular"
    return RecommendResult(
        items=diversified[:size],
        strategy=strategy,
        target_difficulty=target,
    )
