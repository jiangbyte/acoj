"""Contest clarifications: public broadcasts + private Q&A threads."""

from datetime import datetime

from sqlalchemy import Boolean, DateTime, Index, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjContestClarification(Base, TimestampMixin):
    __tablename__ = "oj_contest_clarification"
    __table_args__ = (Index("ix_oj_contest_clarification_contest", "contest_id"),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, nullable=False, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="竞赛ID")
    problem_id: Mapped[str | None] = mapped_column(String(64), comment="关联题目")
    title: Mapped[str] = mapped_column(String(200), nullable=False, comment="标题")
    body: Mapped[str] = mapped_column(Text, nullable=False, comment="正文")
    published_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, comment="发布时间")


class OjContestClarificationThread(Base, TimestampMixin):
    __tablename__ = "oj_contest_clarification_thread"
    __table_args__ = (
        Index("ix_oj_contest_clar_thread_contest", "contest_id"),
        Index("ix_oj_contest_clar_thread_status", "contest_id", "status"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, nullable=False, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="竞赛ID")
    problem_id: Mapped[str | None] = mapped_column(String(64), comment="关联题目")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="提问账户")
    title: Mapped[str] = mapped_column(String(200), nullable=False, comment="标题")
    status: Mapped[str] = mapped_column(String(16), nullable=False, comment="OPEN|ANSWERED|CLOSED")


class OjContestClarificationMessage(Base, TimestampMixin):
    __tablename__ = "oj_contest_clarification_message"
    __table_args__ = (Index("ix_oj_contest_clar_msg_thread", "thread_id"),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, nullable=False, default=generate_snowflake_id, comment="主键"
    )
    thread_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="线程ID")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="发送账户")
    body: Mapped[str] = mapped_column(Text, nullable=False, comment="正文")
    is_staff: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False, comment="是否工作人员回复")
