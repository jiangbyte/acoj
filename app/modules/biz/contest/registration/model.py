"""Contest registration model."""

from datetime import datetime

from sqlalchemy import DateTime, Index, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjContestRegistration(Base, TimestampMixin):
    __tablename__ = "oj_contest_registration"
    __table_args__ = (
        UniqueConstraint("contest_id", "account_id", name="uq_oj_contest_registration_contest_account"),
        Index("ix_oj_contest_registration_contest_status", "contest_id", "status"),
        Index("ix_oj_contest_registration_account", "account_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, nullable=False, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="竞赛ID")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    status: Mapped[str] = mapped_column(String(16), nullable=False, comment="PENDING|APPROVED|REJECTED|CANCELLED")
    source: Mapped[str] = mapped_column(String(16), nullable=False, comment="SELF|ADMIN")
    applied_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, comment="申请时间")
    reviewed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="审核时间")
    reviewed_by: Mapped[str | None] = mapped_column(String(64), comment="审核人")
    remark: Mapped[str | None] = mapped_column(Text, comment="备注/拒绝原因")
