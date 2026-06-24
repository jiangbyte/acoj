from datetime import datetime

from sqlalchemy import Boolean, DateTime, Float, Index, Integer, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjSubmission(Base, TimestampMixin):
    """提交记录表。"""

    __tablename__ = "oj_submission"
    __table_args__ = (
        Index(
            "ix_oj_submission_problem_account_points_submitted_at",
            "problem_id",
            "account_id",
            "points",
            "submitted_at",
        ),
        Index(
            "ix_oj_submission_contest_problem_account_points_submitted_at",
            "contest_id",
            "problem_id",
            "account_id",
            "points",
            "submitted_at",
        ),
        Index("ix_oj_submission_result_id", "result", "id"),
        Index("ix_oj_submission_result_language_id", "result", "language_id", "id"),
        Index("ix_oj_submission_language_id", "language_id", "id"),
        Index("ix_oj_submission_result_problem", "result", "problem_id"),
        Index("ix_oj_submission_language_problem_result", "language_id", "problem_id", "result"),
        Index("ix_oj_submission_problem_result", "problem_id", "result"),
        Index("ix_oj_submission_account_problem_result", "account_id", "problem_id", "result"),
        Index("ix_oj_submission_account_result", "account_id", "result"),
        Index("ix_oj_submission_status", "status"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目")
    submitted_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, comment="提交时间"
    )
    execution_time: Mapped[float | None] = mapped_column(Float, comment="执行时间")
    memory_usage: Mapped[float | None] = mapped_column(Float, comment="内存占用")
    points: Mapped[float | None] = mapped_column(Float, comment="获得分数")
    language_id: Mapped[str] = mapped_column(
        String(64), nullable=False, comment="提交语言"
    )
    status: Mapped[str] = mapped_column(String(2), default="QU", nullable=False, comment="状态")
    result: Mapped[str | None] = mapped_column(String(3), comment="结果")
    error: Mapped[str | None] = mapped_column(Text, comment="编译错误")
    current_test_case: Mapped[int] = mapped_column(
        Integer, default=0, nullable=False, comment="当前测试用例"
    )
    batch_number: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否批量测试点"
    )
    test_case_points: Mapped[float] = mapped_column(
        Float, default=0, nullable=False, comment="测试用例得分"
    )
    test_case_total: Mapped[float] = mapped_column(
        Float, default=0, nullable=False, comment="测试用例总分"
    )
    judge_id: Mapped[str | None] = mapped_column(String(64), comment="评测机ID")
    judged_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), comment="提交评测时间"
    )
    rejudged_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), comment="管理员最近重测时间"
    )
    is_pretested: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否已运行预评测"
    )
    contest_id: Mapped[str | None] = mapped_column(String(64), comment="比赛")
    locked_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), comment="提交锁定时间"
    )
    is_archived: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否归档"
    )


class OjSubmissionSource(Base, TimestampMixin):
    """提交源码表。"""

    __tablename__ = "oj_submission_source"
    __table_args__ = (
        UniqueConstraint("submission_id", name="uq_oj_submission_source_submission_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    submission_id: Mapped[str] = mapped_column(
        String(64), nullable=False, comment="关联提交"
    )
    source: Mapped[str] = mapped_column(Text, nullable=False, comment="源码")


class OjSubmissionTestCase(Base, TimestampMixin):
    """提交测试用例结果表。"""

    __tablename__ = "oj_submission_test_case"
    __table_args__ = (
        UniqueConstraint(
            "submission_id", "test_case", name="uq_oj_submission_test_case_submission_case"
        ),
        Index("ix_oj_submission_test_case_submission", "submission_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    submission_id: Mapped[str] = mapped_column(
        String(64), nullable=False, comment="关联提交"
    )
    test_case: Mapped[int] = mapped_column(Integer, nullable=False, comment="测试用例ID")
    status: Mapped[str] = mapped_column(String(3), nullable=False, comment="状态标识")
    execution_time: Mapped[float | None] = mapped_column(Float, comment="执行时间")
    memory_usage: Mapped[float | None] = mapped_column(Float, comment="内存占用")
    points: Mapped[float | None] = mapped_column(Float, comment="获得分数")
    total: Mapped[float | None] = mapped_column(Float, comment="总分")
    batch_number: Mapped[int | None] = mapped_column(Integer, comment="批次编号")
    feedback: Mapped[str] = mapped_column(
        String(50), default="", nullable=False, comment="评测反馈"
    )
    extended_feedback: Mapped[str] = mapped_column(
        Text, default="", nullable=False, comment="扩展反馈"
    )
    output: Mapped[str] = mapped_column(Text, default="", nullable=False, comment="程序输出")
