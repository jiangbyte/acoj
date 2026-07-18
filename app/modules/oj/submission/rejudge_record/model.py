"""OJ submission rejudge_record model."""

from datetime import datetime

from sqlalchemy import JSON, DateTime, Float, Index, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.oj.enums import (
    OjJudgeTaskStatus,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjRejudgeRecord(Base, TimestampMixin):
    """重测记录。"""

    __tablename__ = "oj_rejudge_record"
    __table_args__ = (Index("ix_oj_rejudge_record_submission", "submission_id", "id"),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    submission_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="提交ID")
    operator_account_type: Mapped[str | None] = mapped_column(String(32), comment="操作账户类型")
    operator_account_id: Mapped[str | None] = mapped_column(String(64), comment="操作账户ID")
    reason: Mapped[str | None] = mapped_column(Text, comment="原因")
    old_result: Mapped[str | None] = mapped_column(String(32), comment="旧结果")
    new_result: Mapped[str | None] = mapped_column(String(32), comment="新结果")
    old_score: Mapped[float | None] = mapped_column(Float, comment="旧分数")
    new_score: Mapped[float | None] = mapped_column(Float, comment="新分数")
    started_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="开始时间")
    finished_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), comment="完成时间"
    )
    status: Mapped[str] = mapped_column(
        String(32),
        default=OjJudgeTaskStatus.PENDING.value,
        nullable=False,
        comment="重测状态",
    )
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")
