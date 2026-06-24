from datetime import datetime

from sqlalchemy import Boolean, DateTime, Index, Integer, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjComment(Base, TimestampMixin):
    """评论表。"""

    __tablename__ = "oj_comment"
    __table_args__ = (
        Index("ix_oj_comment_page", "page"),
        Index("ix_oj_comment_parent", "parent_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    author_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="评论人")
    posted_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, comment="发布时间"
    )
    page: Mapped[str] = mapped_column(String(30), nullable=False, comment="关联页面")
    score: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="投票数")
    body: Mapped[str] = mapped_column(Text, nullable=False, comment="评论内容")
    hidden: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False, comment="是否隐藏")
    parent_id: Mapped[str | None] = mapped_column(String(64), comment="父级")
    revisions: Mapped[int] = mapped_column(Integer, default=1, nullable=False, comment="修订次数")
    left_value: Mapped[int] = mapped_column(Integer, nullable=False, comment="嵌套集合左值")
    right_value: Mapped[int] = mapped_column(Integer, nullable=False, comment="嵌套集合右值")
    tree_id: Mapped[int] = mapped_column(Integer, nullable=False, comment="嵌套集合树ID")
    level: Mapped[int] = mapped_column(Integer, nullable=False, comment="嵌套集合层级")


class OjCommentVote(Base, TimestampMixin):
    """评论投票表。"""

    __tablename__ = "oj_comment_vote"
    __table_args__ = (
        UniqueConstraint("voter_id", "comment_id", name="uq_oj_comment_vote_voter_comment"),
        Index("ix_oj_comment_vote_comment", "comment_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    voter_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="投票人")
    comment_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="评论")
    score: Mapped[int] = mapped_column(Integer, nullable=False, comment="得分")


class OjCommentLock(Base, TimestampMixin):
    """评论锁定表。"""

    __tablename__ = "oj_comment_lock"
    __table_args__ = (UniqueConstraint("page", name="uq_oj_comment_lock_page"),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    page: Mapped[str] = mapped_column(String(30), nullable=False, comment="关联页面")
