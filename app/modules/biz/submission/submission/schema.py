from datetime import datetime

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.biz.submission.enums import SubmissionKind, SubmissionStatus


class OjSubmissionCaseSchema(ApiSchema):
    id: str
    submission_id: str
    case_no: int
    test_case_id: str | None = None
    result: str | None = None
    score: float = 0
    time_ms: int = 0
    memory_kb: int = 0
    stdout_preview: str | None = None
    stderr_preview: str | None = None
    feedback: str | None = None


class OjContestSubmissionSchema(ApiSchema):
    id: str
    submission_id: str
    contest_problem_id: str
    participation_id: str
    points: float = 0
    is_pretest: bool = False


class OjSubmissionListSchema(ApiSchema):
    id: str
    user_id: str
    user_nickname: str | None = None
    user_avatar: str | None = None
    user_account_type: str | None = None
    problem_id: str
    problem_code: str | None = None
    problem_name: str | None = None
    language_key: str
    kind: str
    status: str
    result: str | None = None
    score: float = 0
    time_ms: int = 0
    memory_kb: int = 0
    contest_id: str | None = None
    contest_key: str | None = None
    contest_name: str | None = None
    case_points: float = 0
    case_total: float = 0
    locked_at: datetime | None = None
    created_at: datetime
    updated_at: datetime


class OjSubmissionDetailSchema(OjSubmissionListSchema):
    compile_output: str | None = None
    error: str | None = None
    source: str | None = None
    cases: list[OjSubmissionCaseSchema] = Field(default_factory=list)
    contest_submission: OjContestSubmissionSchema | None = None


class OjSubmissionAdminPageQuery(PageQuery):
    problem_id: str | None = None
    problem_code: str | None = None
    contest_id: str | None = None
    user_id: str | None = None
    kind: SubmissionKind | None = None
    status: SubmissionStatus | None = None
    result: str | None = None
    language_key: str | None = None


class OjSubmissionRejudgeRequest(ApiSchema):
    ids: list[str] = Field(min_length=1)
    wait_timeout_sec: int = Field(default=60, ge=5, le=300)
    wait: bool = False


class OjSubmissionRejudgeResult(ApiSchema):
    queued: int = 0
    completed: int = 0
    failed: int = 0
    errors: list[str] = Field(default_factory=list)
