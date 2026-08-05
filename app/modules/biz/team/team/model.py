"""Team models."""

from datetime import datetime
from typing import Any

from sqlalchemy import DateTime, Index, Integer, JSON, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjTeam(Base, TimestampMixin):
    __tablename__ = "oj_team"
    __table_args__ = (
        UniqueConstraint("invite_code", name="uq_oj_team_invite_code"),
        Index("ix_oj_team_scope", "scope"),
        Index("ix_oj_team_course_id", "course_id"),
        Index("ix_oj_team_owner_id", "owner_id"),
        Index("ix_oj_team_status", "status"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    scope: Mapped[str] = mapped_column(String(16), nullable=False, comment="INDEPENDENT|COURSE")
    course_id: Mapped[str | None] = mapped_column(String(64), comment="课内小组所属课程")
    class_id: Mapped[str | None] = mapped_column(String(64), comment="课内小组所属班级(冗余)")
    name: Mapped[str] = mapped_column(String(200), nullable=False, comment="小组名称")
    description: Mapped[str | None] = mapped_column(Text, comment="简介")
    owner_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="Portal 所有者")
    invite_code: Mapped[str] = mapped_column(String(8), nullable=False, comment="邀请码")
    im_group_id: Mapped[str | None] = mapped_column(String(64), comment="绑定 IM 群")
    status: Mapped[str] = mapped_column(String(16), nullable=False, default="ENABLED", comment="ENABLED|DISABLED|DISSOLVED")
    visibility: Mapped[str] = mapped_column(String(16), nullable=False, default="PRIVATE", comment="PUBLIC|PRIVATE")
    max_members: Mapped[int] = mapped_column(Integer, nullable=False, default=50, comment="最大成员数")
    member_count: Mapped[int] = mapped_column(Integer, nullable=False, default=1, comment="成员数")
    extra: Mapped[dict[str, Any]] = mapped_column(JSON, nullable=False, default=dict)


class OjTeamMember(Base, TimestampMixin):
    __tablename__ = "oj_team_member"
    __table_args__ = (
        UniqueConstraint("team_id", "account_id", name="uq_oj_team_member_team_account"),
        Index("ix_oj_team_member_team_id", "team_id"),
        Index("ix_oj_team_member_account_id", "account_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    team_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="小组ID")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="Portal 账户ID")
    role: Mapped[str] = mapped_column(String(16), nullable=False, default="MEMBER", comment="OWNER|ADMIN|MEMBER")
    joined_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, comment="加入时间")
    left_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="离开时间")
