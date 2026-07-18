"""OJ submission case model."""

from sqlalchemy import Float, Index, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.oj.enums import (
    OjJudgeResult,
    OjSubmitStatus,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjSubmissionCase(Base, TimestampMixin):
    """提交测试点结果。"""

    __tablename__ = "oj_submission_case"
    __table_args__ = (
        Index("ix_oj_submission_case_submission_sort", "submission_id", "sort"),
        Index("uq_oj_submission_case", "submission_id", "case_no", unique=True),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    submission_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="提交ID")
    case_no: Mapped[int] = mapped_column(Integer, nullable=False, comment="测试点编号")
    status: Mapped[str] = mapped_column(
        String(32),
        default=OjSubmitStatus.QUEUED.value,
        nullable=False,
        comment="测试点状态",
    )
    result: Mapped[str | None] = mapped_column(
        String(32), comment=f"判题结果：{OjJudgeResult.__doc__}"
    )
    time_ms: Mapped[int | None] = mapped_column(Integer, comment="耗时毫秒")
    memory_kb: Mapped[int | None] = mapped_column(Integer, comment="内存KB")
    points: Mapped[float | None] = mapped_column(Float, comment="得分")
    total: Mapped[float | None] = mapped_column(Float, comment="总分")
    batch_no: Mapped[int | None] = mapped_column(Integer, comment="批次编号")
    feedback: Mapped[str | None] = mapped_column(String(255), comment="反馈")
    extended_feedback: Mapped[str | None] = mapped_column(Text, comment="扩展反馈")
    output: Mapped[str | None] = mapped_column(Text, comment="程序输出")
    stderr: Mapped[str | None] = mapped_column(Text, comment="错误输出")
    sort: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="排序")
