"""OJ community clarification model."""

from datetime import datetime

from sqlalchemy import DateTime, Index, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.oj.enums import (
    OjClarificationStatus,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjClarification(Base, TimestampMixin):
    """比赛和题目答疑。"""

    __tablename__ = "oj_clarification"
    __table_args__ = (
        Index("ix_oj_clarification_contest_problem", "contest_id", "problem_id"),
        Index("ix_oj_clarification_status", "status"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str | None] = mapped_column(String(64), comment="比赛ID")
    problem_id: Mapped[str | None] = mapped_column(String(64), comment="题目ID")
    question_account_type: Mapped[str] = mapped_column(
        String(32), nullable=False, comment="提问账户类型"
    )
    question_account_id: Mapped[str] = mapped_column(
        String(64), nullable=False, comment="提问账户ID"
    )
    question: Mapped[str] = mapped_column(Text, nullable=False, comment="问题")
    answer: Mapped[str | None] = mapped_column(Text, comment="回答")
    answer_account_type: Mapped[str | None] = mapped_column(String(32), comment="回答账户类型")
    answer_account_id: Mapped[str | None] = mapped_column(String(64), comment="回答账户ID")
    status: Mapped[str] = mapped_column(
        String(32),
        default=OjClarificationStatus.OPEN.value,
        nullable=False,
        comment=f"状态：{OjClarificationStatus.__doc__}",
    )
    asked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="提问时间")
    answered_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), comment="回答时间"
    )
