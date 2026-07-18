"""OJ community announcement model."""

from datetime import datetime

from sqlalchemy import JSON, Boolean, DateTime, Index, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.oj.enums import (
    OjAnnouncementScope,
    OjContentStatus,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjAnnouncement(Base, TimestampMixin):
    """OJ 和比赛公告。"""

    __tablename__ = "oj_announcement"
    __table_args__ = (
        Index("ix_oj_announcement_scope_status", "scope", "status", "publish_at"),
        Index("ix_oj_announcement_contest", "contest_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    scope: Mapped[str] = mapped_column(
        String(32),
        default=OjAnnouncementScope.GLOBAL.value,
        nullable=False,
        comment=f"公告范围：{OjAnnouncementScope.__doc__}",
    )
    contest_id: Mapped[str | None] = mapped_column(String(64), comment="比赛ID")
    title: Mapped[str] = mapped_column(String(255), nullable=False, comment="标题")
    content: Mapped[str] = mapped_column(Text, nullable=False, comment="内容")
    status: Mapped[str] = mapped_column(
        String(32),
        default=OjContentStatus.DRAFT.value,
        nullable=False,
        comment=f"状态：{OjContentStatus.__doc__}",
    )
    publish_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="发布时间")
    pinned: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False, comment="是否置顶")
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")
