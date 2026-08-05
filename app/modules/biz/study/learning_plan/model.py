"""Models: learning plan."""

from typing import Any

from sqlalchemy import JSON, Index, Integer, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjLearningPlan(Base, TimestampMixin):
    __tablename__ = "oj_learning_plan"
    __table_args__ = (
        UniqueConstraint("code", name="uq_oj_learning_plan_code"),
        Index("ix_oj_learning_plan_category_status", "category", "status"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    code: Mapped[str] = mapped_column(String(64), nullable=False)
    title: Mapped[str] = mapped_column(String(200), nullable=False)
    subtitle: Mapped[str | None] = mapped_column(String(255))
    overview: Mapped[str | None] = mapped_column(Text)
    cover_url: Mapped[str | None] = mapped_column(String(512))
    category: Mapped[str] = mapped_column(String(32), nullable=False)
    status: Mapped[str] = mapped_column(String(16), nullable=False, default="ENABLED")
    sort: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    extra: Mapped[dict[str, Any]] = mapped_column(JSON, nullable=False, default=dict)


class OjLearningPlanSection(Base, TimestampMixin):
    __tablename__ = "oj_learning_plan_section"
    __table_args__ = (Index("ix_oj_learning_plan_section_plan_id", "plan_id"),)

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    plan_id: Mapped[str] = mapped_column(String(64), nullable=False)
    title: Mapped[str] = mapped_column(String(200), nullable=False)
    sort: Mapped[int] = mapped_column(Integer, nullable=False, default=0)


class OjLearningPlanItem(Base, TimestampMixin):
    __tablename__ = "oj_learning_plan_item"
    __table_args__ = (
        UniqueConstraint("section_id", "problem_id", name="uq_oj_learning_plan_item_section_problem"),
        Index("ix_oj_learning_plan_item_section_id", "section_id"),
        Index("ix_oj_learning_plan_item_problem_id", "problem_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    section_id: Mapped[str] = mapped_column(String(64), nullable=False)
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False)
    sort: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
