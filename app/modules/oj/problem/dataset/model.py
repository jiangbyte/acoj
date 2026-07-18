"""OJ problem dataset model."""

from sqlalchemy import JSON, Boolean, Index, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjDataset(Base, TimestampMixin):
    """题目测试数据集。"""

    __tablename__ = "oj_dataset"
    __table_args__ = (
        Index("ix_oj_dataset_problem_active", "problem_id", "is_active"),
        Index("uq_oj_dataset_problem_version", "problem_id", "version", unique=True),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目ID")
    name: Mapped[str] = mapped_column(String(128), nullable=False, comment="数据集名称")
    version: Mapped[str] = mapped_column(String(64), nullable=False, comment="数据集版本")
    is_active: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否启用"
    )
    data_zip_url: Mapped[str | None] = mapped_column(String(1024), comment="数据包地址")
    generator_url: Mapped[str | None] = mapped_column(String(1024), comment="生成器地址")
    checker: Mapped[str | None] = mapped_column(String(64), comment="检查器")
    checker_args: Mapped[dict] = mapped_column(
        JSON, default=dict, nullable=False, comment="检查器参数"
    )
    output_prefix: Mapped[int | None] = mapped_column(Integer, comment="输出前缀长度")
    output_limit: Mapped[int | None] = mapped_column(Integer, comment="输出限制长度")
    unicode_enabled: Mapped[bool | None] = mapped_column(Boolean, comment="是否启用 unicode")
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")
