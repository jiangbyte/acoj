"""OJ contest participation model."""

from datetime import datetime

from sqlalchemy import JSON, Boolean, DateTime, Float, Index, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.oj.enums import (
    OjParticipationType,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjContestParticipation(Base, TimestampMixin):
    """参赛记录。"""

    __tablename__ = "oj_contest_participation"
    __table_args__ = (
        Index("ix_oj_contest_participation_rank", "contest_id", "rank"),
        Index(
            "uq_oj_contest_participation_account",
            "contest_id",
            "account_type",
            "account_id",
            "participation_type",
            unique=True,
        ),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛ID")
    account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="账户类型")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    participation_type: Mapped[str] = mapped_column(
        String(32),
        default=OjParticipationType.LIVE.value,
        nullable=False,
        comment=f"参赛类型：{OjParticipationType.__doc__}",
    )
    started_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="开始时间")
    ended_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="结束时间")
    score: Mapped[float] = mapped_column(Float, default=0.0, nullable=False, comment="得分")
    penalty: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="罚时")
    rank: Mapped[int | None] = mapped_column(Integer, comment="排名")
    is_disqualified: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否取消资格"
    )
    format_data: Mapped[dict] = mapped_column(
        JSON, default=dict, nullable=False, comment="赛制数据"
    )
