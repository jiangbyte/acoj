from datetime import datetime

from sqlalchemy import JSON, Boolean, DateTime, Float, Index, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.judge.enums import JudgeNodeStatus, JudgeTaskStatus, SubmissionStatus
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjJudgeNode(Base, TimestampMixin):
    __tablename__ = "oj_judge_node"
    __table_args__ = (
        Index("ix_oj_judge_node_status_enabled", "status", "enabled"),
        Index("ix_oj_judge_node_last_heartbeat", "last_heartbeat_at"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    node_id: Mapped[str] = mapped_column(String(128), unique=True, nullable=False)
    name: Mapped[str] = mapped_column(String(128), nullable=False)
    base_url: Mapped[str | None] = mapped_column(String(512), nullable=True)
    version: Mapped[str] = mapped_column(String(64), nullable=False, default="unknown")
    status: Mapped[str] = mapped_column(
        String(32), nullable=False, default=JudgeNodeStatus.ONLINE.value
    )
    enabled: Mapped[bool] = mapped_column(Boolean, nullable=False, default=True)
    cpu_core: Mapped[int] = mapped_column(Integer, nullable=False, default=1)
    load: Mapped[float] = mapped_column(Float, nullable=False, default=0)
    running_tasks: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    supported_languages: Mapped[list[str]] = mapped_column(JSON, nullable=False, default=list)
    supported_features: Mapped[list[str]] = mapped_column(JSON, nullable=False, default=list)
    last_heartbeat_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


class OjJudgeTask(Base, TimestampMixin):
    __tablename__ = "oj_judge_task"
    __table_args__ = (
        Index("ix_oj_judge_task_status_priority_created", "status", "priority", "created_at"),
        Index("ix_oj_judge_task_submission", "submission_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    submission_id: Mapped[str] = mapped_column(String(64), nullable=False)
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False)
    problem_version: Mapped[str] = mapped_column(String(64), nullable=False, default="default")
    node_id: Mapped[str | None] = mapped_column(String(128), nullable=True)
    status: Mapped[str] = mapped_column(
        String(32), nullable=False, default=JudgeTaskStatus.PENDING.value
    )
    priority: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    attempt: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    payload: Mapped[dict] = mapped_column(JSON, nullable=False, default=dict)
    locked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    started_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    finished_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    error_message: Mapped[str | None] = mapped_column(Text)


class OjSubmission(Base, TimestampMixin):
    __tablename__ = "oj_submission"
    __table_args__ = (
        Index("ix_oj_submission_problem_user", "problem_id", "user_id"),
        Index("ix_oj_submission_status_created", "status", "created_at"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False)
    user_id: Mapped[str] = mapped_column(String(64), nullable=False)
    language: Mapped[str] = mapped_column(String(64), nullable=False)
    source: Mapped[str] = mapped_column(Text, nullable=False)
    status: Mapped[str] = mapped_column(
        String(32), nullable=False, default=SubmissionStatus.PENDING.value
    )
    score: Mapped[float] = mapped_column(Float, nullable=False, default=0)
    time_ms: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    memory_kb: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    compile_message: Mapped[str | None] = mapped_column(Text)
    judger_id: Mapped[str | None] = mapped_column(String(128))


class OjSubmissionCase(Base, TimestampMixin):
    __tablename__ = "oj_submission_case"
    __table_args__ = (Index("ix_oj_submission_case_submission", "submission_id", "case_no"),)

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    submission_id: Mapped[str] = mapped_column(String(64), nullable=False)
    case_no: Mapped[int] = mapped_column(Integer, nullable=False)
    batch_no: Mapped[int | None] = mapped_column(Integer)
    status: Mapped[str] = mapped_column(String(32), nullable=False)
    score: Mapped[float] = mapped_column(Float, nullable=False, default=0)
    total_score: Mapped[float] = mapped_column(Float, nullable=False, default=0)
    time_ms: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    memory_kb: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    feedback: Mapped[str | None] = mapped_column(Text)
    output_preview: Mapped[str | None] = mapped_column(Text)

