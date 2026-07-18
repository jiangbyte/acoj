"""OJ judge runtime_version model."""

from sqlalchemy import Index, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjRuntimeVersion(Base, TimestampMixin):
    """判题机运行时版本。"""

    __tablename__ = "oj_runtime_version"
    __table_args__ = (
        Index("ix_oj_runtime_version_judge_language", "judge_node_id", "language_id"),
        Index("uq_oj_runtime_version", "judge_node_id", "language_id", "runtime_name", unique=True),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    judge_node_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="判题机ID")
    language_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="语言ID")
    runtime_name: Mapped[str] = mapped_column(String(128), nullable=False, comment="运行时名称")
    runtime_version: Mapped[str | None] = mapped_column(String(128), comment="运行时版本")
    priority: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="优先级")
