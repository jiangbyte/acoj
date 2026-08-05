from datetime import datetime
from typing import Literal

from pydantic import Field

from pydantic import Field

from app.core.schema.base import ApiSchema, IdQuery, ProblemIdQuery

PerformanceScope = Literal["practice", "contest"]


class PerformanceBucketOut(ApiSchema):
    start: float
    end: float
    count: int
    is_current: bool = False


class SubmissionPerformanceOut(ApiSchema):
    available: bool
    reason: str | None = None
    scope: PerformanceScope | None = None
    problem_id: str | None = None
    language_key: str | None = None
    contest_id: str | None = None
    time_ms: int | None = None
    memory_kb: int | None = None
    sample_size: int | None = None
    insufficient_sample: bool | None = None
    beats_time_pct: float | None = None
    beats_memory_pct: float | None = None
    runtime_buckets: list[PerformanceBucketOut] | None = None
    memory_buckets: list[PerformanceBucketOut] | None = None


class SimilarSubmissionItem(ApiSchema):
    id: str
    user_id: str
    nickname: str | None = None
    avatar: str | None = None
    language_key: str
    time_ms: int
    memory_kb: int
    created_at: datetime
    source: str | None = None


class SimilarSubmissionListOut(ApiSchema):
    available: bool
    reason: str | None = None
    items: list[SimilarSubmissionItem] = Field(default_factory=list)


class SimilarSubmissionQuery(IdQuery):
    size: int = Field(default=10, ge=1, le=50)


class SubmissionEventsQuery(IdQuery):
    max_wait_sec: int = Field(default=120, ge=5, le=600)


class MyLatestPracticeAcQuery(ProblemIdQuery):
    pass


class MyLatestPracticeAcOut(ApiSchema):
    submission_id: str | None = None


class MySubmissionStatsOut(ApiSchema):
    """当前登录用户的正式练习提交汇总（排除 TRIAL）。"""

    submission_total: int = 0
    ac_total: int = 0
    fail_total: int = 0
    judging_total: int = 0
    ac_rate: float = 0
    solved_problem_total: int = 0
