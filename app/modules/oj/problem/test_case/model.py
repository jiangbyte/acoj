"""OJ problem test_case model."""

from sqlalchemy import JSON, Boolean, Float, Index, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.oj.enums import (
    OjTestCaseType,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjTestCase(Base, TimestampMixin):
    """测试点。"""

    __tablename__ = "oj_test_case"
    __table_args__ = (
        Index("ix_oj_test_case_dataset_sort", "dataset_id", "sort"),
        Index("uq_oj_test_case_dataset_case", "dataset_id", "case_no", unique=True),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    dataset_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="数据集ID")
    case_no: Mapped[int] = mapped_column(Integer, nullable=False, comment="测试点编号")
    case_type: Mapped[str] = mapped_column(
        String(32),
        default=OjTestCaseType.NORMAL.value,
        nullable=False,
        comment=f"测试点类型：{OjTestCaseType.__doc__}",
    )
    input_file: Mapped[str | None] = mapped_column(String(255), comment="输入文件")
    output_file: Mapped[str | None] = mapped_column(String(255), comment="输出文件")
    input_inline: Mapped[str | None] = mapped_column(Text, comment="内联输入")
    output_inline: Mapped[str | None] = mapped_column(Text, comment="内联输出")
    generator_args: Mapped[str | None] = mapped_column(Text, comment="生成器参数")
    points: Mapped[float | None] = mapped_column(Float, comment="分值")
    is_pretest: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否预评测"
    )
    batch_no: Mapped[int | None] = mapped_column(Integer, comment="批次编号")
    batch_dependencies: Mapped[list[int]] = mapped_column(
        JSON, default=list, nullable=False, comment="批次依赖"
    )
    time_limit_ms: Mapped[int | None] = mapped_column(Integer, comment="时间限制毫秒")
    memory_limit_kb: Mapped[int | None] = mapped_column(Integer, comment="内存限制KB")
    checker: Mapped[str | None] = mapped_column(String(64), comment="检查器")
    checker_args: Mapped[dict] = mapped_column(
        JSON, default=dict, nullable=False, comment="检查器参数"
    )
    sort: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="排序")
