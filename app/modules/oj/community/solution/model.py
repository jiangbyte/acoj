"""OJ community solution model."""

from datetime import datetime

from sqlalchemy import JSON, BigInteger, DateTime, Index, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.oj.enums import (
    OjContentStatus,
    OjContentType,
    OjProblemVisibility,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjSolution(Base, TimestampMixin):
    """题解。"""

    __tablename__ = "oj_solution"
    __table_args__ = (
        Index("ix_oj_solution_problem_status", "problem_id", "status"),
        Index("ix_oj_solution_account", "account_type", "account_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目ID")
    account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="账户类型")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    title: Mapped[str] = mapped_column(String(255), nullable=False, comment="标题")
    content: Mapped[str] = mapped_column(Text, nullable=False, comment="内容")
    content_type: Mapped[str] = mapped_column(
        String(32),
        default=OjContentType.MARKDOWN.value,
        nullable=False,
        comment=f"内容格式：{OjContentType.__doc__}",
    )
    visibility: Mapped[str] = mapped_column(
        String(32),
        default=OjProblemVisibility.PUBLIC.value,
        nullable=False,
        comment="可见性",
    )
    status: Mapped[str] = mapped_column(
        String(32),
        default=OjContentStatus.DRAFT.value,
        nullable=False,
        comment=f"状态：{OjContentStatus.__doc__}",
    )
    publish_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="发布时间")
    view_count: Mapped[int] = mapped_column(BigInteger, default=0, nullable=False, comment="浏览数")
    like_count: Mapped[int] = mapped_column(BigInteger, default=0, nullable=False, comment="点赞数")
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")
