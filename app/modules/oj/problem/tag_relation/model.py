"""OJ problem tag_relation model."""

from sqlalchemy import Index, String
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjProblemTagRelation(Base):
    """题目标签关系。"""

    __tablename__ = "oj_problem_tag_relation"
    __table_args__ = (
        Index("ix_oj_problem_tag_relation_problem", "problem_id"),
        Index("uq_oj_problem_tag_relation", "problem_id", "tag_id", unique=True),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目ID")
    tag_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="标签ID")
