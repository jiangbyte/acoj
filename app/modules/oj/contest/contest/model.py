"""OJ contest contest model."""

from datetime import datetime

from sqlalchemy import JSON, Boolean, DateTime, Index, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.core.config.enums import StatusEnum
from app.modules.oj.enums import (
    OjContestFormat,
    OjContestVisibility,
    OjScoreboardVisibility,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjContest(Base, TimestampMixin):
    """比赛主表。"""

    __tablename__ = "oj_contest"
    __table_args__ = (
        Index("ix_oj_contest_key", "key", unique=True),
        Index("ix_oj_contest_status_time", "status", "start_at", "end_at"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    key: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛编码")
    name: Mapped[str] = mapped_column(String(255), nullable=False, comment="比赛名称")
    description: Mapped[str | None] = mapped_column(Text, comment="描述")
    summary: Mapped[str | None] = mapped_column(String(500), comment="摘要")
    start_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, comment="开始时间"
    )
    end_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, comment="结束时间"
    )
    duration_seconds: Mapped[int | None] = mapped_column(Integer, comment="参赛时长秒")
    visibility: Mapped[str] = mapped_column(
        String(32),
        default=OjContestVisibility.PUBLIC.value,
        nullable=False,
        comment=f"可见性：{OjContestVisibility.__doc__}",
    )
    contest_format: Mapped[str] = mapped_column(
        String(32),
        default=OjContestFormat.ICPC.value,
        nullable=False,
        comment=f"赛制：{OjContestFormat.__doc__}",
    )
    format_config: Mapped[dict] = mapped_column(
        JSON, default=dict, nullable=False, comment="赛制配置"
    )
    scoreboard_visibility: Mapped[str] = mapped_column(
        String(32),
        default=OjScoreboardVisibility.VISIBLE.value,
        nullable=False,
        comment=f"榜单可见性：{OjScoreboardVisibility.__doc__}",
    )
    is_rated: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否计分评级"
    )
    rating_floor: Mapped[int | None] = mapped_column(Integer, comment="评级下限")
    rating_ceiling: Mapped[int | None] = mapped_column(Integer, comment="评级上限")
    access_code_hash: Mapped[str | None] = mapped_column(String(255), comment="访问码哈希")
    allow_virtual: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="允许虚拟参赛"
    )
    freeze_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="封榜时间")
    unfreeze_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), comment="解封时间"
    )
    status: Mapped[str] = mapped_column(
        String(32),
        default=StatusEnum.ENABLED.value,
        nullable=False,
        comment="状态",
    )
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")
