from datetime import datetime

from pydantic import Field

from app.core.schema.base import ApiSchema


class JudgeNodeRegisterRequest(ApiSchema):
    node_id: str = Field(min_length=1, max_length=128)
    name: str = Field(min_length=1, max_length=128)
    version: str = "unknown"
    base_url: str | None = None
    cpu_core: int = 1
    supported_languages: list[str] = Field(default_factory=list)
    supported_features: list[str] = Field(default_factory=list)


class JudgeNodeHeartbeatRequest(JudgeNodeRegisterRequest):
    load: float = 0
    running_tasks: int = 0


class JudgeTaskPollRequest(ApiSchema):
    node_id: str = Field(min_length=1, max_length=128)
    capacity: int = 1


class JudgeTaskEventRequest(ApiSchema):
    type: str = Field(min_length=1, max_length=64)
    payload: dict = Field(default_factory=dict)


class JudgeTaskFinishRequest(ApiSchema):
    status: str
    result: dict = Field(default_factory=dict)
    error_message: str | None = None


class JudgeNodeSchema(ApiSchema):
    id: str
    node_id: str
    name: str
    base_url: str | None
    version: str
    status: str
    enabled: bool
    cpu_core: int
    load: float
    running_tasks: int
    supported_languages: list[str]
    supported_features: list[str]
    last_heartbeat_at: datetime | None


class JudgeTaskSchema(ApiSchema):
    id: str
    submission_id: str
    problem_id: str
    problem_version: str
    node_id: str | None
    status: str
    priority: int
    attempt: int
    payload: dict
