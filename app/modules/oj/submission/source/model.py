"""OJ submission source model."""

from sqlalchemy import JSON, BigInteger, Index, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjSubmissionSource(Base, TimestampMixin):
    """提交源码或提交答案文件引用。"""

    __tablename__ = "oj_submission_source"
    __table_args__ = (Index("ix_oj_submission_source_submission", "submission_id", unique=True),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    submission_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="提交ID")
    source: Mapped[str | None] = mapped_column(Text, comment="源码")
    source_hash: Mapped[str | None] = mapped_column(String(128), comment="源码哈希")
    answer_files: Mapped[list[dict]] = mapped_column(
        JSON, default=list, nullable=False, comment="提交答案文件"
    )
    size: Mapped[int | None] = mapped_column(BigInteger, comment="大小")
