"""Learning plan schemas."""

from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.biz.study.enums import LearningPlanCategory
from app.modules.biz.study.problem_list_schema import ProblemListProblemBrief, ProblemListProgress


class LearningPlanSectionInput(ApiSchema):
    id: str | None = None
    title: str
    sort: int = 0
    problem_ids: list[str] = Field(default_factory=list)


class LearningPlanCreateRequest(ApiSchema):
    code: str = Field(min_length=1, max_length=64)
    title: str
    subtitle: str | None = None
    overview: str | None = None
    cover_url: str | None = None
    category: LearningPlanCategory = LearningPlanCategory.FEATURED
    sort: int = 0
    status: str = "ENABLED"
    sections: list[LearningPlanSectionInput] = Field(default_factory=list)
    extra: dict[str, Any] | None = Field(default_factory=dict)


class LearningPlanUpdateRequest(LearningPlanCreateRequest):
    id: str


class LearningPlanAdminPageQuery(ApiSchema):
    pagination: PageQuery
    title: str | None = None
    code: str | None = None
    category: LearningPlanCategory | None = None
    status: str | None = None


class LearningPlanSectionSchema(ApiSchema):
    id: str
    title: str
    sort: int
    problems: list[ProblemListProblemBrief] = Field(default_factory=list)


class LearningPlanSchema(ApiSchema):
    id: str
    code: str
    title: str
    subtitle: str | None = None
    overview: str | None = None
    cover_url: str | None = None
    category: str
    status: str
    sort: int
    problem_count: int = 0
    progress: ProblemListProgress | None = None
    sections: list[LearningPlanSectionSchema] = Field(default_factory=list)
    related: list["LearningPlanSchema"] = Field(default_factory=list)
    extra: dict[str, Any] | None = Field(default_factory=dict)
    created_at: datetime
    updated_at: datetime
