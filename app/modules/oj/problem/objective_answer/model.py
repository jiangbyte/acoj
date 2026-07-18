"""OJ problem objective_answer model."""

from sqlalchemy import JSON, Index, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.oj.enums import (
    OjObjectiveAnswerType,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjObjectiveAnswer(Base, TimestampMixin):
    """客观题答案配置。"""

    __tablename__ = "oj_objective_answer"
    __table_args__ = (Index("ix_oj_objective_answer_problem", "problem_id"),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目ID")
    answer_type: Mapped[str] = mapped_column(
        String(32),
        default=OjObjectiveAnswerType.SINGLE.value,
        nullable=False,
        comment=f"答案类型：{OjObjectiveAnswerType.__doc__}",
    )
    answer: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="答案")
    score_rule: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="计分规则")
    explanation: Mapped[str | None] = mapped_column(Text, comment="解析")
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")
