"""Contest core model."""

from datetime import datetime
from typing import Any

from sqlalchemy import JSON, Boolean, DateTime, Index, Integer, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjContest(Base, TimestampMixin):
    __tablename__ = "oj_contest"
    __table_args__ = (
        UniqueConstraint("key", name="uq_oj_contest_key"),
        Index("ix_oj_contest_is_visible", "is_visible"),
        Index("ix_oj_contest_start_time", "start_time"),
        Index("ix_oj_contest_end_time", "end_time"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, nullable=False, default=generate_snowflake_id, comment="主键"
    )
    key: Mapped[str] = mapped_column(String(32), nullable=False, comment="竞赛标识")
    name: Mapped[str] = mapped_column(String(200), nullable=False, comment="竞赛名称")
    description: Mapped[str] = mapped_column(Text, nullable=False, comment="竞赛说明")
    summary: Mapped[str | None] = mapped_column(Text, comment="摘要")
    start_time: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, comment="开始时间")
    end_time: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, comment="结束时间")
    time_limit_seconds: Mapped[int | None] = mapped_column(Integer, comment="个人参赛时长（秒）")
    freeze_seconds: Mapped[int | None] = mapped_column(Integer, comment="结束前封榜秒数")
    is_visible: Mapped[bool] = mapped_column(Boolean, nullable=False, comment="是否公开可见")
    is_private: Mapped[bool] = mapped_column(Boolean, nullable=False, comment="是否仅限指定选手")
    access_code: Mapped[str | None] = mapped_column(String(255), comment="参赛准入码")
    is_rated: Mapped[bool] = mapped_column(Boolean, nullable=False, comment="是否计入 Rating")
    rating_floor: Mapped[int | None] = mapped_column(Integer, comment="Rating 下限")
    rating_ceiling: Mapped[int | None] = mapped_column(Integer, comment="Rating 上限")
    rate_all: Mapped[bool] = mapped_column(Boolean, nullable=False, comment="无提交也计 Rating")
    scoreboard_visibility: Mapped[str] = mapped_column(String(32), nullable=False, comment="榜单可见性")
    format_name: Mapped[str] = mapped_column(String(32), nullable=False, comment="赛制")
    format_config: Mapped[dict[str, Any] | None] = mapped_column(JSON, comment="赛制配置")
    points_precision: Mapped[int] = mapped_column(Integer, nullable=False, comment="分数小数精度")
    hide_problem_tags: Mapped[bool] = mapped_column(Boolean, nullable=False, comment="赛中隐藏题目标签")
    hide_problem_authors: Mapped[bool] = mapped_column(Boolean, nullable=False, comment="赛中隐藏命题人")
    run_pretests_only: Mapped[bool] = mapped_column(Boolean, nullable=False, comment="赛中仅跑 pretest")
    use_clarifications: Mapped[bool] = mapped_column(Boolean, nullable=False, comment="使用答疑")
    tester_see_scoreboard: Mapped[bool] = mapped_column(Boolean, nullable=False, comment="测试员可见榜单")
    tester_see_submissions: Mapped[bool] = mapped_column(Boolean, nullable=False, comment="测试员可见提交")
    locked_after: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="重判锁定时间")
    register_start: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="报名开始时间")
    register_end: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="报名截止时间")
    registration_mode: Mapped[str] = mapped_column(
        String(16), nullable=False, default="AUTO", server_default="AUTO", comment="报名模式 AUTO|REVIEW"
    )
    list_visibility: Mapped[str] = mapped_column(
        String(16), nullable=False, default="PUBLIC", server_default="PUBLIC", comment="列表可见性 PUBLIC|INVITE_ONLY"
    )
    user_count: Mapped[int] = mapped_column(Integer, nullable=False, default=0, comment="正式参赛人数")
    extra: Mapped[dict[str, Any]] = mapped_column(JSON, nullable=False, default=dict, comment="扩展信息")
