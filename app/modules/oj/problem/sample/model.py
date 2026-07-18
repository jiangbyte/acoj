"""OJ problem sample model."""

from sqlalchemy import Index, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjProblemSample(Base, TimestampMixin):
    """题目样例。"""

    __tablename__ = "oj_problem_sample"
    __table_args__ = (Index("ix_oj_problem_sample_problem_sort", "problem_id", "sort"),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目ID")
    input: Mapped[str | None] = mapped_column(Text, comment="样例输入")
    output: Mapped[str | None] = mapped_column(Text, comment="样例输出")
    explanation: Mapped[str | None] = mapped_column(Text, comment="样例说明")
    sort: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="排序")
