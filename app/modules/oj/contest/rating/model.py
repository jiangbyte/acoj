"""OJ contest rating model."""

from datetime import datetime

from sqlalchemy import DateTime, Float, Index, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjContestRating(Base, TimestampMixin):
    """比赛评级结果。"""

    __tablename__ = "oj_contest_rating"
    __table_args__ = (
        Index("ix_oj_contest_rating_contest_rank", "contest_id", "rank"),
        Index(
            "uq_oj_contest_rating_account", "contest_id", "account_type", "account_id", unique=True
        ),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛ID")
    participation_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="参赛记录ID")
    account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="账户类型")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    rank: Mapped[int] = mapped_column(Integer, nullable=False, comment="排名")
    old_rating: Mapped[int | None] = mapped_column(Integer, comment="旧评级")
    new_rating: Mapped[int | None] = mapped_column(Integer, comment="新评级")
    performance: Mapped[float | None] = mapped_column(Float, comment="表现分")
    rated_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="评级时间")
