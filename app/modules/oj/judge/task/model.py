"""OJ judge task model."""

from datetime import datetime

from sqlalchemy import JSON, DateTime, Index, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.oj.enums import (
    OjJudgeTaskStatus,
    OjJudgeTaskType,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjJudgeTask(Base, TimestampMixin):
    """判题任务队列。"""

    __tablename__ = "oj_judge_task"
    __table_args__ = (
        Index("ix_oj_judge_task_status_priority", "status", "priority", "id"),
        Index("ix_oj_judge_task_submission", "submission_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    submission_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="提交ID")
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目ID")
    judge_node_id: Mapped[str | None] = mapped_column(String(64), comment="判题机ID")
    task_type: Mapped[str] = mapped_column(
        String(32),
        default=OjJudgeTaskType.JUDGE.value,
        nullable=False,
        comment=f"任务类型：{OjJudgeTaskType.__doc__}",
    )
    priority: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="优先级")
    status: Mapped[str] = mapped_column(
        String(32),
        default=OjJudgeTaskStatus.PENDING.value,
        nullable=False,
        comment=f"任务状态：{OjJudgeTaskStatus.__doc__}",
    )
    attempts: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="尝试次数")
    locked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="锁定时间")
    started_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="开始时间")
    finished_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), comment="完成时间"
    )
    error: Mapped[str | None] = mapped_column(Text, comment="错误")
    payload: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="任务载荷")
    result_payload: Mapped[dict] = mapped_column(
        JSON, default=dict, nullable=False, comment="结果载荷"
    )
