"""OJ community vote model."""

from sqlalchemy import Index, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.oj.enums import (
    OjVoteTargetType,
    OjVoteType,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjVote(Base, TimestampMixin):
    """点赞和投票。"""

    __tablename__ = "oj_vote"
    __table_args__ = (
        Index("ix_oj_vote_target", "target_type", "target_id"),
        Index(
            "uq_oj_vote_account",
            "target_type",
            "target_id",
            "account_type",
            "account_id",
            "vote_type",
            unique=True,
        ),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    target_type: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        comment=f"目标类型：{OjVoteTargetType.__doc__}",
    )
    target_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="目标ID")
    account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="账户类型")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    vote_type: Mapped[str] = mapped_column(
        String(32),
        default=OjVoteType.LIKE.value,
        nullable=False,
        comment=f"投票类型：{OjVoteType.__doc__}",
    )
    score: Mapped[int] = mapped_column(Integer, default=1, nullable=False, comment="分数")
