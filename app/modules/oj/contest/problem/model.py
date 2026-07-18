"""OJ contest problem model."""

from sqlalchemy import Boolean, Float, Index, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjContestProblem(Base, TimestampMixin):
    """比赛题目关系。"""

    __tablename__ = "oj_contest_problem"
    __table_args__ = (
        Index("ix_oj_contest_problem_contest_sort", "contest_id", "sort"),
        Index("uq_oj_contest_problem", "contest_id", "problem_id", unique=True),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛ID")
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目ID")
    label: Mapped[str | None] = mapped_column(String(32), comment="比赛题号")
    points: Mapped[float] = mapped_column(Float, default=100.0, nullable=False, comment="分值")
    partial: Mapped[bool] = mapped_column(
        Boolean, default=True, nullable=False, comment="是否允许部分分"
    )
    is_pretest: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否只跑预评测"
    )
    max_submissions: Mapped[int | None] = mapped_column(Integer, comment="最大提交次数")
    sort: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="排序")
