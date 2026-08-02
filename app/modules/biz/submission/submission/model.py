"""OJ submission models (practice + contest + admin trial)."""

from datetime import datetime
from typing import Any

from sqlalchemy import Boolean, DateTime, Float, ForeignKey, Index, Integer, String, Text, UniqueConstraint
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjSubmission(Base, TimestampMixin):
    __tablename__ = "oj_submission"
    __table_args__ = (
        Index("ix_oj_submission_problem_user_created", "problem_id", "user_id", "created_at"),
        Index("ix_oj_submission_contest_problem_user_score", "contest_id", "problem_id", "user_id", "score"),
        Index("ix_oj_submission_user_result", "user_id", "result"),
        Index("ix_oj_submission_kind_created", "kind", "created_at"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, nullable=False, default=generate_snowflake_id, comment="主键"
    )
    user_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="提交账户ID")
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目ID")
    language_key: Mapped[str] = mapped_column(String(32), nullable=False, comment="语言 key")
    kind: Mapped[str] = mapped_column(String(16), nullable=False, comment="OFFICIAL|TRIAL")
    status: Mapped[str] = mapped_column(String(16), nullable=False, comment="QUEUED|JUDGING|COMPLETED|FAILED")
    result: Mapped[str | None] = mapped_column(String(16), comment="AC|WA|TLE|…")
    score: Mapped[float] = mapped_column(Float, nullable=False, default=0, comment="得分")
    time_ms: Mapped[int] = mapped_column(Integer, nullable=False, default=0, comment="耗时 ms")
    memory_kb: Mapped[int] = mapped_column(Integer, nullable=False, default=0, comment="内存 KB")
    compile_output: Mapped[str | None] = mapped_column(Text, comment="编译输出")
    error: Mapped[str | None] = mapped_column(Text, comment="错误信息")
    contest_id: Mapped[str | None] = mapped_column(String(64), comment="竞赛ID（denorm）")
    case_points: Mapped[float] = mapped_column(Float, nullable=False, default=0, comment="测例得分合计")
    case_total: Mapped[float] = mapped_column(Float, nullable=False, default=0, comment="测例满分合计")
    locked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="锁定后禁止重判")

    source_row: Mapped["OjSubmissionSource | None"] = relationship(
        back_populates="submission",
        uselist=False,
        cascade="all, delete-orphan",
        lazy="raise",
    )
    cases: Mapped[list["OjSubmissionCase"]] = relationship(
        back_populates="submission",
        cascade="all, delete-orphan",
        lazy="raise",
        order_by="OjSubmissionCase.case_no",
    )
    contest_submission: Mapped["OjContestSubmission | None"] = relationship(
        back_populates="submission",
        uselist=False,
        cascade="all, delete-orphan",
        lazy="raise",
    )


class OjSubmissionSource(Base, TimestampMixin):
    __tablename__ = "oj_submission_source"

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, nullable=False, default=generate_snowflake_id, comment="主键"
    )
    submission_id: Mapped[str] = mapped_column(
        String(64),
        ForeignKey("oj_submission.id", ondelete="CASCADE"),
        nullable=False,
        unique=True,
        comment="提交ID",
    )
    source: Mapped[str] = mapped_column(Text, nullable=False, comment="源代码")

    submission: Mapped[OjSubmission] = relationship(back_populates="source_row", lazy="raise")


class OjSubmissionCase(Base, TimestampMixin):
    __tablename__ = "oj_submission_case"
    __table_args__ = (UniqueConstraint("submission_id", "case_no", name="uq_oj_submission_case_no"),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, nullable=False, default=generate_snowflake_id, comment="主键"
    )
    submission_id: Mapped[str] = mapped_column(
        String(64),
        ForeignKey("oj_submission.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
        comment="提交ID",
    )
    case_no: Mapped[int] = mapped_column(Integer, nullable=False, comment="测例编号")
    test_case_id: Mapped[str | None] = mapped_column(String(64), comment="oj_problem_test_case.id")
    result: Mapped[str | None] = mapped_column(String(16), comment="测例 verdict")
    score: Mapped[float] = mapped_column(Float, nullable=False, default=0, comment="测例得分")
    time_ms: Mapped[int] = mapped_column(Integer, nullable=False, default=0, comment="耗时 ms")
    memory_kb: Mapped[int] = mapped_column(Integer, nullable=False, default=0, comment="内存 KB")
    stdout_preview: Mapped[str | None] = mapped_column(Text, comment="stdout 预览")
    stderr_preview: Mapped[str | None] = mapped_column(Text, comment="stderr 预览")
    feedback: Mapped[str | None] = mapped_column(String(255), comment="短反馈")

    submission: Mapped[OjSubmission] = relationship(back_populates="cases", lazy="raise")


class OjContestSubmission(Base, TimestampMixin):
    __tablename__ = "oj_contest_submission"

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, nullable=False, default=generate_snowflake_id, comment="主键"
    )
    submission_id: Mapped[str] = mapped_column(
        String(64),
        ForeignKey("oj_submission.id", ondelete="CASCADE"),
        nullable=False,
        unique=True,
        comment="提交ID",
    )
    contest_problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="竞赛题目ID")
    participation_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="参赛记录ID")
    points: Mapped[float] = mapped_column(Float, nullable=False, default=0, comment="竞赛换算分")
    is_pretest: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False, comment="是否仅 pretest")
    extra: Mapped[dict[str, Any] | None] = mapped_column(JSONB, comment="扩展")

    submission: Mapped[OjSubmission] = relationship(back_populates="contest_submission", lazy="raise")
