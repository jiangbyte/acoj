"""Models: daily problem."""

from datetime import date

from sqlalchemy import Date, Index, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjDailyProblem(Base, TimestampMixin):
    __tablename__ = "oj_daily_problem"
    __table_args__ = (
        UniqueConstraint("day_date", name="uq_oj_daily_problem_day_date"),
        Index("ix_oj_daily_problem_problem_id", "problem_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    day_date: Mapped[date] = mapped_column(Date, nullable=False)
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False)
