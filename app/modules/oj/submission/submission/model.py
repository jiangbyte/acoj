"""OJ submission submission model."""

from datetime import datetime

from sqlalchemy import JSON, Boolean, DateTime, Float, Index, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.oj.enums import (
    OjJudgeMode,
    OjJudgeResult,
    OjSubmitStatus,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjSubmission(Base, TimestampMixin):
    """提交记录。"""

    __tablename__ = "oj_submission"
    __table_args__ = (
        Index("ix_oj_submission_problem_account", "problem_id", "account_type", "account_id", "id"),
        Index("ix_oj_submission_result_language", "result", "language_id", "id"),
        Index("ix_oj_submission_contest", "contest_id", "participation_id", "contest_problem_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目ID")
    problem_code: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目编码快照")
    account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="账户类型")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    language_id: Mapped[str | None] = mapped_column(String(64), comment="语言ID")
    judge_mode: Mapped[str] = mapped_column(
        String(32),
        default=OjJudgeMode.STANDARD.value,
        nullable=False,
        comment=f"判题方式：{OjJudgeMode.__doc__}",
    )
    status: Mapped[str] = mapped_column(
        String(32),
        default=OjSubmitStatus.QUEUED.value,
        nullable=False,
        comment=f"提交状态：{OjSubmitStatus.__doc__}",
    )
    result: Mapped[str | None] = mapped_column(
        String(32), comment=f"判题结果：{OjJudgeResult.__doc__}"
    )
    score: Mapped[float | None] = mapped_column(Float, comment="得分")
    time_ms: Mapped[int | None] = mapped_column(Integer, comment="耗时毫秒")
    memory_kb: Mapped[int | None] = mapped_column(Integer, comment="内存KB")
    current_case: Mapped[int] = mapped_column(
        Integer, default=0, nullable=False, comment="当前测试点"
    )
    case_points: Mapped[float] = mapped_column(
        Float, default=0.0, nullable=False, comment="测试点得分"
    )
    case_total: Mapped[float] = mapped_column(
        Float, default=0.0, nullable=False, comment="测试点总分"
    )
    compile_output: Mapped[str | None] = mapped_column(Text, comment="编译输出")
    judge_node_id: Mapped[str | None] = mapped_column(String(64), comment="判题机ID")
    submitted_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), comment="提交时间"
    )
    judged_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="判题时间")
    rejudged_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), comment="重测时间"
    )
    contest_id: Mapped[str | None] = mapped_column(String(64), comment="比赛ID")
    contest_problem_id: Mapped[str | None] = mapped_column(String(64), comment="比赛题目ID")
    participation_id: Mapped[str | None] = mapped_column(String(64), comment="参赛记录ID")
    is_pretest: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否预评测"
    )
    is_archived: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否归档"
    )
    source_visibility: Mapped[str | None] = mapped_column(String(32), comment="源码可见性")
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")
