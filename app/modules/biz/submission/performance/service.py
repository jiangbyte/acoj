"""Pool aggregation, beats %, histograms, and similar submissions."""

from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.biz.problem.enums import SubmissionSourceVisibility
from app.modules.biz.problem.problem.model import OjProblem
from app.modules.biz.submission.enums import SubmissionKind, SubmissionResult, SubmissionStatus
from app.modules.biz.submission.performance.schema import (
    PerformanceBucketOut,
    PerformanceScope,
    SimilarSubmissionItem,
    SimilarSubmissionListOut,
    SubmissionPerformanceOut,
)
from app.modules.biz.submission.submission.model import OjSubmission, OjSubmissionSource
from app.modules.biz.submission.submission.repository import OjSubmissionRepository
from app.modules.biz.submission.submission.service import OjSubmissionService

MIN_SAMPLE_SIZE = 5
DEFAULT_MAX_BUCKETS = 20
DEFAULT_SIMILAR_SIZE = 10


@dataclass(frozen=True)
class PoolRow:
    id: str
    user_id: str
    time_ms: int
    memory_kb: int
    created_at: datetime


def compute_beats_pct(current: int, metrics: list[int]) -> float | None:
    """Strictly-better ratio: count(metric > current) / sample_size * 100."""
    sample_size = len(metrics)
    if sample_size < MIN_SAMPLE_SIZE:
        return None
    better = sum(1 for value in metrics if value > current)
    return 100.0 * better / sample_size


def build_histogram_buckets(
    metrics: list[int],
    current: int,
    *,
    max_buckets: int = DEFAULT_MAX_BUCKETS,
) -> list[PerformanceBucketOut] | None:
    """Equal-width histogram over [min, max]; mark the bucket containing `current`."""
    sample_size = len(metrics)
    if sample_size < MIN_SAMPLE_SIZE:
        return None

    num_buckets = min(max_buckets, sample_size)
    min_val = min(metrics)
    max_val = max(metrics)

    if min_val == max_val:
        return [
            PerformanceBucketOut(
                start=float(min_val),
                end=float(max_val),
                count=sample_size,
                is_current=True,
            )
        ]

    width = (max_val - min_val) / num_buckets
    counts = [0] * num_buckets
    for value in metrics:
        idx = min(int((value - min_val) / width), num_buckets - 1)
        counts[idx] += 1

    current_idx = min(int((current - min_val) / width), num_buckets - 1)
    buckets: list[PerformanceBucketOut] = []
    for i in range(num_buckets):
        start = min_val + i * width
        end = max_val if i == num_buckets - 1 else min_val + (i + 1) * width
        buckets.append(
            PerformanceBucketOut(
                start=float(start),
                end=float(end),
                count=counts[i],
                is_current=i == current_idx,
            )
        )
    return buckets


class SubmissionPerformanceService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjSubmissionRepository(db)
        self.submission_service = OjSubmissionService(db)

    async def get_performance(
        self,
        submission_id: str,
        *,
        viewer: str | None,
        for_admin: bool,
    ) -> SubmissionPerformanceOut:
        submission = await self.repo.get_by_id(submission_id)
        if submission is None:
            raise NotFoundError("提交不存在")

        pool = self._resolve_pool(submission, for_admin=for_admin)
        if pool is None:
            return SubmissionPerformanceOut(
                available=False,
                reason=self._unavailable_reason(submission, for_admin=for_admin),
            )

        scope, contest_id = pool
        rows = await self._load_pool_rows(submission, scope=scope, contest_id=contest_id)
        time_metrics = [row.time_ms for row in rows]
        memory_metrics = [row.memory_kb for row in rows]
        sample_size = len(rows)
        insufficient = sample_size < MIN_SAMPLE_SIZE

        out = SubmissionPerformanceOut(
            available=True,
            scope=scope,
            problem_id=submission.problem_id,
            language_key=submission.language_key,
            contest_id=contest_id,
            time_ms=submission.time_ms,
            memory_kb=submission.memory_kb,
            sample_size=sample_size,
            insufficient_sample=insufficient,
        )
        if insufficient:
            return out

        out.beats_time_pct = compute_beats_pct(submission.time_ms, time_metrics)
        out.beats_memory_pct = compute_beats_pct(submission.memory_kb, memory_metrics)
        out.runtime_buckets = build_histogram_buckets(time_metrics, submission.time_ms)
        out.memory_buckets = build_histogram_buckets(memory_metrics, submission.memory_kb)
        return out

    async def list_similar(
        self,
        submission_id: str,
        *,
        size: int = DEFAULT_SIMILAR_SIZE,
        viewer: str | None,
        for_admin: bool,
    ) -> SimilarSubmissionListOut:
        submission = await self.repo.get_by_id(submission_id)
        if submission is None:
            raise NotFoundError("提交不存在")

        pool = self._resolve_pool(submission, for_admin=for_admin)
        if pool is None:
            return SimilarSubmissionListOut(
                available=False,
                reason=self._unavailable_reason(submission, for_admin=for_admin),
            )

        scope, contest_id = pool
        rows = await self._load_pool_rows(submission, scope=scope, contest_id=contest_id)
        candidates = [
            row
            for row in rows
            if row.id != submission.id and row.user_id != submission.user_id
        ]
        candidates.sort(
            key=lambda row: (
                abs(row.time_ms - submission.time_ms) + abs(row.memory_kb - submission.memory_kb),
                -row.created_at.timestamp(),
            )
        )
        picked = candidates[: max(size, 0)]

        profile_map = await self.submission_service._batch_user_profiles([row.user_id for row in picked])
        source_map = await self._load_sources([row.id for row in picked])

        items: list[SimilarSubmissionItem] = []
        for row in picked:
            profile = profile_map.get(row.user_id) or {}
            source: str | None = source_map.get(row.id)
            if not for_admin:
                if not await self._can_view_source(submission.problem_id, row.user_id, viewer):
                    source = None
            items.append(
                SimilarSubmissionItem(
                    id=row.id,
                    user_id=row.user_id,
                    nickname=profile.get("nickname"),
                    avatar=profile.get("avatar"),
                    language_key=submission.language_key,
                    time_ms=row.time_ms,
                    memory_kb=row.memory_kb,
                    created_at=row.created_at,
                    source=source,
                )
            )

        return SimilarSubmissionListOut(available=True, items=items)

    async def my_latest_practice_ac(self, user_id: str, problem_id: str) -> str | None:
        stmt = (
            select(OjSubmission.id)
            .where(
                OjSubmission.user_id == user_id,
                OjSubmission.problem_id == problem_id,
                OjSubmission.kind == SubmissionKind.OFFICIAL.value,
                OjSubmission.contest_id.is_(None),
                OjSubmission.status == SubmissionStatus.COMPLETED.value,
                OjSubmission.result == SubmissionResult.AC.value,
            )
            .order_by(OjSubmission.created_at.desc(), OjSubmission.id.desc())
            .limit(1)
        )
        return (await self.db.execute(stmt)).scalar_one_or_none()

    def _resolve_pool(
        self,
        submission: OjSubmission,
        *,
        for_admin: bool,
    ) -> tuple[PerformanceScope, str | None] | None:
        if submission.kind == SubmissionKind.TRIAL.value:
            return None
        if submission.status != SubmissionStatus.COMPLETED.value:
            return None
        if submission.result != SubmissionResult.AC.value:
            return None

        if for_admin and submission.kind == SubmissionKind.CONTEST.value and submission.contest_id:
            return ("contest", submission.contest_id)

        if submission.kind == SubmissionKind.OFFICIAL.value and submission.contest_id is None:
            return ("practice", None)

        return None

    def _unavailable_reason(self, submission: OjSubmission, *, for_admin: bool) -> str:
        if submission.kind == SubmissionKind.TRIAL.value:
            return "试判提交不支持表现统计"
        if submission.status != SubmissionStatus.COMPLETED.value:
            return "提交尚未判题完成"
        if submission.result != SubmissionResult.AC.value:
            return "仅 AC 提交可查看表现统计"
        if not for_admin and submission.kind == SubmissionKind.CONTEST.value:
            return "竞赛提交请使用竞赛详情查看"
        if submission.contest_id is not None and submission.kind == SubmissionKind.OFFICIAL.value:
            return "非官方练习提交"
        return "当前提交不适用表现统计"

    async def _load_pool_rows(
        self,
        submission: OjSubmission,
        *,
        scope: PerformanceScope,
        contest_id: str | None,
    ) -> list[PoolRow]:
        stmt = select(
            OjSubmission.id,
            OjSubmission.user_id,
            OjSubmission.time_ms,
            OjSubmission.memory_kb,
            OjSubmission.created_at,
        ).where(
            OjSubmission.problem_id == submission.problem_id,
            OjSubmission.language_key == submission.language_key,
            OjSubmission.status == SubmissionStatus.COMPLETED.value,
            OjSubmission.result == SubmissionResult.AC.value,
            OjSubmission.kind != SubmissionKind.TRIAL.value,
        )
        if scope == "practice":
            stmt = stmt.where(
                OjSubmission.kind == SubmissionKind.OFFICIAL.value,
                OjSubmission.contest_id.is_(None),
            )
        else:
            stmt = stmt.where(
                OjSubmission.kind == SubmissionKind.CONTEST.value,
                OjSubmission.contest_id == contest_id,
            )
        rows = (await self.db.execute(stmt)).all()
        return [
            PoolRow(
                id=row.id,
                user_id=row.user_id,
                time_ms=row.time_ms,
                memory_kb=row.memory_kb,
                created_at=row.created_at,
            )
            for row in rows
        ]

    async def _load_sources(self, submission_ids: list[str]) -> dict[str, str]:
        unique = list(dict.fromkeys(sid for sid in submission_ids if sid))
        if not unique:
            return {}
        stmt = select(OjSubmissionSource.submission_id, OjSubmissionSource.source).where(
            OjSubmissionSource.submission_id.in_(unique)
        )
        rows = (await self.db.execute(stmt)).all()
        return {row.submission_id: row.source for row in rows}

    async def _can_view_source(
        self,
        problem_id: str,
        owner_id: str,
        viewer_account_id: str | None,
    ) -> bool:
        """Mirror PortalSubmissionService source visibility rules."""
        if viewer_account_id and viewer_account_id == owner_id:
            return True
        problem = await self.db.get(OjProblem, problem_id)
        if problem is None:
            return False
        vis = problem.submission_source_visibility or SubmissionSourceVisibility.ONLY_OWN.value
        if vis == SubmissionSourceVisibility.ALWAYS.value:
            return True
        if vis == SubmissionSourceVisibility.ONLY_OWN.value:
            return False
        if vis == SubmissionSourceVisibility.FOLLOW.value:
            return False
        if vis == SubmissionSourceVisibility.SOLVED.value:
            if not viewer_account_id:
                return False
            return await self._user_solved(problem_id, viewer_account_id)
        return False

    async def _user_solved(self, problem_id: str, account_id: str) -> bool:
        row = (
            await self.db.execute(
                select(OjSubmission.id)
                .where(
                    OjSubmission.problem_id == problem_id,
                    OjSubmission.user_id == account_id,
                    OjSubmission.status == SubmissionStatus.COMPLETED.value,
                    OjSubmission.result == SubmissionResult.AC.value,
                    OjSubmission.kind != SubmissionKind.TRIAL.value,
                )
                .limit(1)
            )
        ).scalar_one_or_none()
        return row is not None
