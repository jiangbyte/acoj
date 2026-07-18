"""OJ problem member model."""

from sqlalchemy import Index, String
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.oj.enums import (
    OjProblemMemberRole,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjProblemMember(Base, TimestampMixin):
    """题目成员和访问关系。"""

    __tablename__ = "oj_problem_member"
    __table_args__ = (
        Index("ix_oj_problem_member_problem_role", "problem_id", "role"),
        Index(
            "uq_oj_problem_member_account",
            "problem_id",
            "account_type",
            "account_id",
            "role",
            unique=True,
        ),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目ID")
    account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="账户类型")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    role: Mapped[str] = mapped_column(
        String(32),
        default=OjProblemMemberRole.VIEWER.value,
        nullable=False,
        comment=f"成员角色：{OjProblemMemberRole.__doc__}",
    )
