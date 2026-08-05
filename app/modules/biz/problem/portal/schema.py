"""Portal problem bank schemas."""

from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.response.pagination import PageData, PageQuery
from app.core.schema.base import ApiSchema
from app.modules.biz.problem.enums import ProblemDifficulty, SubmissionSourceVisibility


class PortalProblemPageQuery(PageQuery):
    keyword: str | None = None
    code: str | None = None
    name: str | None = None
    group_id: str | None = None
    type_id: str | None = None


class PortalProblemListSchema(ApiSchema):
    id: str
    code: str
    name: str
    summary: str | None = None
    group_id: str | None = None
    group_name: str | None = None
    time_limit_ms: int
    memory_limit_kb: int
    points: float
    partial: bool
    difficulty: ProblemDifficulty = ProblemDifficulty.MEDIUM
    user_count: int
    ac_rate: float
    solved: bool = False
    type_ids: list[str] = Field(default_factory=list)
    type_names: list[str] = Field(default_factory=list)


class PortalProblemPageData(PageData[PortalProblemListSchema]):
    solved_count: int = 0


class PortalProblemRecommendItem(PortalProblemListSchema):
    """推荐结果：列表字段 + 可解释原因。"""

    reason: str = "综合推荐"
    score: float = 0.0


class PortalProblemRecommendData(ApiSchema):
    records: list[PortalProblemRecommendItem] = Field(default_factory=list)
    strategy: str = "personalized"
    target_difficulty: str | None = None


class PortalProblemDetailSchema(PortalProblemListSchema):
    description: str
    submission_source_visibility: SubmissionSourceVisibility
    published_at: datetime | None = None
    extra: dict[str, Any] | None = Field(default_factory=dict)


class PortalProblemLanguageSchema(ApiSchema):
    language_key: str
    label: str | None = None
    extension: str | None = None
    time_limit_ms: int | None = None
    memory_limit_kb: int | None = None


class PortalProblemRecommendQuery(ApiSchema):
    size: int = Field(default=8, ge=1, le=50)


class PortalProblemSubmitRequest(ApiSchema):
    problem_id: str = Field(min_length=1, max_length=64)
    language_key: str = Field(min_length=1, max_length=32)
    source: str = Field(min_length=1)
    wait: bool = False
    wait_timeout_sec: int = Field(default=60, ge=5, le=300)


class PortalProblemGroupItem(ApiSchema):
    id: str
    code: str
    name: str
    sort: int
    problem_count: int = 0


class PortalProblemTypeItem(ApiSchema):
    id: str
    code: str
    name: str
    sort: int
    problem_count: int = 0
