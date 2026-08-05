"""Class (clazz) models."""

from datetime import datetime
from typing import Any

from sqlalchemy import DateTime, Index, Integer, JSON, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjClass(Base, TimestampMixin):
    __tablename__ = "oj_class"
    __table_args__ = (
        UniqueConstraint("code", name="uq_oj_class_code"),
        UniqueConstraint("invite_code", name="uq_oj_class_invite_code"),
        Index("ix_oj_class_status", "status"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    code: Mapped[str] = mapped_column(String(64), nullable=False, comment="班级编码")
    name: Mapped[str] = mapped_column(String(200), nullable=False, comment="班级名称")
    summary: Mapped[str | None] = mapped_column(Text, comment="简介")
    invite_code: Mapped[str] = mapped_column(String(8), nullable=False, comment="邀请码")
    status: Mapped[str] = mapped_column(String(16), nullable=False, default="ENABLED", comment="ENABLED|DISABLED")
    visibility: Mapped[str] = mapped_column(String(16), nullable=False, default="PRIVATE", comment="PUBLIC|PRIVATE")
    im_group_id: Mapped[str | None] = mapped_column(String(64), comment="绑定 IM 群")
    member_count: Mapped[int] = mapped_column(Integer, nullable=False, default=0, comment="成员数")
    extra: Mapped[dict[str, Any]] = mapped_column(JSON, nullable=False, default=dict)


class OjClassMember(Base, TimestampMixin):
    __tablename__ = "oj_class_member"
    __table_args__ = (
        UniqueConstraint("class_id", "account_id", name="uq_oj_class_member_class_account"),
        Index("ix_oj_class_member_class_id", "class_id"),
        Index("ix_oj_class_member_account_id", "account_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    class_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="班级ID")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="Portal 账户ID")
    role: Mapped[str] = mapped_column(String(16), nullable=False, default="STUDENT", comment="STUDENT|ASSISTANT")
    joined_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, comment="加入时间")
    left_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="离开时间")
