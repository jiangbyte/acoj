"""Course DTOs."""

from datetime import datetime
from typing import Any

from pydantic import Field, model_validator

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.biz.course.enums import (
    AnnouncementStatus,
    CourseAccessScope,
    CourseBindingMode,
    CourseStatus,
    CourseVisibility,
    ProgressStatus,
    TaskMode,
    TaskStatus,
)


class OjCourseClassBriefSchema(ApiSchema):
    id: str
    code: str
    name: str


class OjCourseCreateRequest(ApiSchema):
    """access_scope=OPEN 公开课（可不绑班）；CLASS 私有课须绑班。"""

    access_scope: CourseAccessScope = CourseAccessScope.CLASS
    class_ids: list[str] = Field(default_factory=list)
    binding_mode: CourseBindingMode = CourseBindingMode.SHARED
    name: str = Field(min_length=1, max_length=200)
    summary: str | None = None
    cover_url: str | None = None
    visibility: CourseVisibility = CourseVisibility.PRIVATE
    sort: int = 0
    extra: dict[str, Any] = Field(default_factory=dict)

    @model_validator(mode="after")
    def _normalize_binding(self):
        ids = list(dict.fromkeys([cid.strip() for cid in self.class_ids if cid and cid.strip()]))
        self.class_ids = ids
        if self.access_scope == CourseAccessScope.OPEN:
            # 公开课不绑班；列表默认公开
            self.class_ids = []
            self.binding_mode = CourseBindingMode.SHARED
            if self.visibility == CourseVisibility.PRIVATE:
                self.visibility = CourseVisibility.PUBLIC
        elif not ids:
            raise ValueError("私有课至少选择一个班级")
        return self


class OjCourseUpdateRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    name: str | None = None
    summary: str | None = None
    cover_url: str | None = None
    visibility: CourseVisibility | None = None
    access_scope: CourseAccessScope | None = None
    sort: int | None = None
    class_ids: list[str] | None = None
    extra: dict[str, Any] | None = None


class OjCourseAdminPageQuery(ApiSchema):
    pagination: PageQuery
    class_id: str | None = None
    name: str | None = None
    status: CourseStatus | None = None
    visibility: CourseVisibility | None = None
    access_scope: CourseAccessScope | None = None
    binding_mode: CourseBindingMode | None = None


class OjCoursePortalPageQuery(ApiSchema):
    pagination: PageQuery
    keyword: str | None = None


class OjCourseSchema(ApiSchema):
    id: str
    name: str
    summary: str | None = None
    cover_url: str | None = None
    status: str
    visibility: str
    access_scope: str
    binding_mode: str
    sort: int
    class_ids: list[str] = Field(default_factory=list)
    classes: list[OjCourseClassBriefSchema] = Field(default_factory=list)
    # 兼容：首个关联班级
    class_id: str | None = None
    can_participate: bool = False
    extra: dict[str, Any]
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None


class OjCourseCreateResult(ApiSchema):
    id: str
    ids: list[str] = Field(default_factory=list)


class OjCourseAnnouncementCreateRequest(ApiSchema):
    course_id: str = Field(min_length=1, max_length=64)
    title: str = Field(min_length=1, max_length=200)
    content: str | None = None


class OjCourseAnnouncementUpdateRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    title: str | None = None
    content: str | None = None
    status: AnnouncementStatus | None = None


class OjCourseAnnouncementSchema(ApiSchema):
    id: str
    course_id: str
    title: str
    content: str | None = None
    status: str
    published_at: datetime | None = None
    created_at: datetime
    updated_at: datetime


class OjCourseTaskCreateRequest(ApiSchema):
    course_id: str = Field(min_length=1, max_length=64)
    title: str = Field(min_length=1, max_length=200)
    description: str | None = None
    mode: TaskMode
    open_at: datetime | None = None
    close_at: datetime | None = None
    due_at: datetime | None = None
    sort: int = 0
    extra: dict[str, Any] = Field(default_factory=dict)

    @model_validator(mode="after")
    def validate_mode_times(self):
        if self.mode == TaskMode.REALTIME:
            if self.open_at is None or self.close_at is None:
                raise ValueError("REALTIME 任务必须设置 open_at 和 close_at")
        return self


class OjCourseTaskUpdateRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    title: str | None = None
    description: str | None = None
    mode: TaskMode | None = None
    open_at: datetime | None = None
    close_at: datetime | None = None
    due_at: datetime | None = None
    sort: int | None = None
    extra: dict[str, Any] | None = None


class OjCourseTaskSetProblemsRequest(ApiSchema):
    task_id: str = Field(min_length=1, max_length=64)
    problem_ids: list[str] = Field(default_factory=list)
    scores: dict[str, float] | None = None


class OjCourseTaskProblemSchema(ApiSchema):
    id: str
    task_id: str
    problem_id: str
    sort: int
    score: float | None = None


class OjCourseTaskProgressSchema(ApiSchema):
    id: str
    task_id: str
    account_id: str
    solved_count: int
    total_count: int
    status: str
    finished_at: datetime | None = None


class OjCourseTaskSchema(ApiSchema):
    id: str
    course_id: str
    title: str
    description: str | None = None
    mode: str
    status: str
    open_at: datetime | None = None
    close_at: datetime | None = None
    due_at: datetime | None = None
    sort: int
    extra: dict[str, Any]
    created_at: datetime
    updated_at: datetime
    problems: list[OjCourseTaskProblemSchema] = Field(default_factory=list)
    my_progress: OjCourseTaskProgressSchema | None = None


class OjCourseTaskRecordSubmissionRequest(ApiSchema):
    task_id: str = Field(min_length=1, max_length=64)
    problem_id: str = Field(min_length=1, max_length=64)
    submission_id: str = Field(min_length=1, max_length=64)


class OjCourseTaskProgressBoardQuery(ApiSchema):
    task_id: str = Field(min_length=1, max_length=64)
