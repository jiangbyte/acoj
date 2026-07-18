"""OJ contest member model."""

from sqlalchemy import Index, String
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.oj.enums import (
    OjContestMemberRole,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjContestMember(Base, TimestampMixin):
    """比赛成员。"""

    __tablename__ = "oj_contest_member"
    __table_args__ = (
        Index("ix_oj_contest_member_contest_role", "contest_id", "role"),
        Index(
            "uq_oj_contest_member_account",
            "contest_id",
            "account_type",
            "account_id",
            "role",
            unique=True,
        ),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛ID")
    account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="账户类型")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    role: Mapped[str] = mapped_column(
        String(32),
        default=OjContestMemberRole.CONTESTANT.value,
        nullable=False,
        comment=f"成员角色：{OjContestMemberRole.__doc__}",
    )
