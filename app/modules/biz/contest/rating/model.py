"""Per-contest rating settlement rows."""

from datetime import datetime

from sqlalchemy import DateTime, Index, Integer, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjContestRating(Base, TimestampMixin):
    __tablename__ = "oj_contest_rating"
    __table_args__ = (
        UniqueConstraint("participation_id", name="uq_oj_contest_rating_participation"),
        UniqueConstraint("contest_id", "account_id", name="uq_oj_contest_rating_contest_account"),
        Index("ix_oj_contest_rating_contest", "contest_id"),
        Index("ix_oj_contest_rating_account", "account_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, nullable=False, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="竞赛ID")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    participation_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="参赛记录ID")
    rank: Mapped[int] = mapped_column(Integer, nullable=False, comment="名次")
    rating: Mapped[int] = mapped_column(Integer, nullable=False, comment="结算后 Rating")
    delta: Mapped[int] = mapped_column(Integer, nullable=False, comment="Rating 变化")
    performance: Mapped[int] = mapped_column(Integer, nullable=False, comment="本场表现分")
    rated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, comment="结算时间")
