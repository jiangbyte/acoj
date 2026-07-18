"""OJ contest problem_result model."""

from datetime import datetime

from sqlalchemy import JSON, Boolean, DateTime, Float, Index, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjContestProblemResult(Base, TimestampMixin):
    """比赛单题榜单聚合。"""

    __tablename__ = "oj_contest_problem_result"
    __table_args__ = (
        Index("ix_oj_contest_problem_result_contest", "contest_id", "participation_id"),
        Index(
            "uq_oj_contest_problem_result", "participation_id", "contest_problem_id", unique=True
        ),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛ID")
    participation_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="参赛记录ID")
    contest_problem_id: Mapped[str] = mapped_column(
        String(64), nullable=False, comment="比赛题目ID"
    )
    best_submission_id: Mapped[str | None] = mapped_column(String(64), comment="最佳提交ID")
    score: Mapped[float] = mapped_column(Float, default=0.0, nullable=False, comment="得分")
    penalty: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="罚时")
    attempts: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="尝试次数")
    accepted_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), comment="通过时间"
    )
    is_first_ac: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否一血"
    )
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")
