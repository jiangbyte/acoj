"""OJ problem language_limit model — 按语言覆盖全部限制。"""

from sqlalchemy import Index, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjLanguageLimit(Base, TimestampMixin):
    """题目按语言限制（时间/内存/栈/输出），一旦设置全部字段必填，不回退到全局限。"""

    __tablename__ = "oj_language_limit"
    __table_args__ = (
        Index("uq_oj_language_limit", "problem_id", "language_id", unique=True),
        Index("ix_oj_language_limit_problem", "problem_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目ID")
    language_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="语言ID")
    time_limit_ms: Mapped[int] = mapped_column(Integer, nullable=False, comment="时间限制毫秒")
    memory_limit_kb: Mapped[int] = mapped_column(Integer, nullable=False, comment="内存限制KB")
    stack_limit_kb: Mapped[int] = mapped_column(Integer, nullable=False, comment="栈限制KB")
    output_limit_kb: Mapped[int] = mapped_column(Integer, nullable=False, comment="输出限制KB")
