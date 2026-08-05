"""Models: problem list."""

from typing import Any

from sqlalchemy import JSON, Boolean, Index, Integer, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjProblemList(Base, TimestampMixin):
    __tablename__ = "oj_problem_list"
    __table_args__ = (
        Index("ix_oj_problem_list_owner_id", "owner_id"),
        Index("ix_oj_problem_list_kind_status", "kind", "status"),
        Index("ix_oj_problem_list_code", "code"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    kind: Mapped[str] = mapped_column(String(16), nullable=False, comment="PERSONAL|OFFICIAL")
    owner_id: Mapped[str | None] = mapped_column(String(64), comment="个人题单所有者")
    code: Mapped[str | None] = mapped_column(String(64), comment="官方题单编码")
    title: Mapped[str] = mapped_column(String(200), nullable=False)
    summary: Mapped[str | None] = mapped_column(Text)
    cover_url: Mapped[str | None] = mapped_column(String(512))
    visibility: Mapped[str] = mapped_column(String(16), nullable=False, comment="PRIVATE|PUBLIC")
    is_system: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    status: Mapped[str] = mapped_column(String(16), nullable=False, default="ENABLED")
    sort: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    extra: Mapped[dict[str, Any]] = mapped_column(JSON, nullable=False, default=dict)


class OjProblemListItem(Base, TimestampMixin):
    __tablename__ = "oj_problem_list_item"
    __table_args__ = (
        UniqueConstraint("list_id", "problem_id", name="uq_oj_problem_list_item_list_problem"),
        Index("ix_oj_problem_list_item_list_id", "list_id"),
        Index("ix_oj_problem_list_item_problem_id", "problem_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    list_id: Mapped[str] = mapped_column(String(64), nullable=False)
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False)
    sort: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
