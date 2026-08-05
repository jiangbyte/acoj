"""Problem list schemas (admin + portal)."""

from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.biz.study.enums import ProblemListKind, ProblemListVisibility


class ProblemListCreateRequest(ApiSchema):
    title: str = Field(min_length=1, max_length=200)
    summary: str | None = None
    cover_url: str | None = None
    visibility: ProblemListVisibility = ProblemListVisibility.PRIVATE
    code: str | None = None
    sort: int = 0
    problem_ids: list[str] = Field(default_factory=list)
    status: str = "ENABLED"


class OfficialProblemListCreateRequest(ProblemListCreateRequest):
    code: str = Field(min_length=1, max_length=64)
    visibility: ProblemListVisibility = ProblemListVisibility.PUBLIC


class ProblemListUpdateRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    title: str = Field(min_length=1, max_length=200)
    summary: str | None = None
    cover_url: str | None = None
    visibility: ProblemListVisibility | None = None
    code: str | None = None
    sort: int | None = None
    status: str | None = None
    problem_ids: list[str] | None = None


class ProblemListItemMutation(ApiSchema):
    list_id: str
    problem_id: str


class ProblemListReorderRequest(ApiSchema):
    list_id: str
    items: list[dict[str, Any]]  # [{problem_id, sort}]


class ProblemListAdminPageQuery(ApiSchema):
    pagination: PageQuery
    title: str | None = None
    code: str | None = None
    kind: ProblemListKind | None = None
    status: str | None = None


class ProblemListProblemBrief(ApiSchema):
    id: str
    code: str
    name: str
    difficulty: str
    ac_rate: float
    user_count: int
    solved: bool = False
    attempted: bool = False
    sort: int = 0


class ProblemListProgress(ApiSchema):
    solved: int = 0
    attempted: int = 0
    total: int = 0
    easy_solved: int = 0
    easy_total: int = 0
    medium_solved: int = 0
    medium_total: int = 0
    hard_solved: int = 0
    hard_total: int = 0


class ProblemListSchema(ApiSchema):
    id: str
    kind: str
    owner_id: str | None = None
    code: str | None = None
    title: str
    summary: str | None = None
    cover_url: str | None = None
    visibility: str
    is_system: bool = False
    status: str
    sort: int
    problem_count: int = 0
    progress: ProblemListProgress | None = None
    problems: list[ProblemListProblemBrief] = Field(default_factory=list)
    extra: dict[str, Any] | None = Field(default_factory=dict)
    created_at: datetime
    updated_at: datetime
