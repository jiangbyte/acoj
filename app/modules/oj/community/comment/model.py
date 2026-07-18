"""OJ community comment model."""

from sqlalchemy import JSON, Index, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.oj.enums import (
    OjCommentTargetType,
    OjContentStatus,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjComment(Base, TimestampMixin):
    """评论。"""

    __tablename__ = "oj_comment"
    __table_args__ = (
        Index("ix_oj_comment_target", "target_type", "target_id"),
        Index("ix_oj_comment_parent", "parent_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    target_type: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        comment=f"目标类型：{OjCommentTargetType.__doc__}",
    )
    target_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="目标ID")
    parent_id: Mapped[str | None] = mapped_column(String(64), comment="父评论ID")
    account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="账户类型")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    content: Mapped[str] = mapped_column(Text, nullable=False, comment="内容")
    status: Mapped[str] = mapped_column(
        String(32),
        default=OjContentStatus.PUBLISHED.value,
        nullable=False,
        comment=f"状态：{OjContentStatus.__doc__}",
    )
    score: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="分数")
    reply_count: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="回复数")
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")
