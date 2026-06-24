from datetime import datetime

from sqlalchemy import Boolean, DateTime, Index, Integer, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjTicket(Base, TimestampMixin):
    """工单表。"""

    __tablename__ = "oj_ticket"
    __table_args__ = (
        Index("ix_oj_ticket_account_open", "account_id", "is_open"),
        Index("ix_oj_ticket_linked_item", "content_type_id", "object_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    title: Mapped[str] = mapped_column(String(100), nullable=False, comment="工单标题")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="工单创建人")
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, comment="创建时间"
    )
    notes: Mapped[str] = mapped_column(Text, default="", nullable=False, comment="快捷备注")
    content_type_id: Mapped[int] = mapped_column(
        Integer, nullable=False, comment="关联对象类型"
    )
    object_id: Mapped[int] = mapped_column(Integer, nullable=False, comment="关联对象ID")
    is_open: Mapped[bool] = mapped_column(
        Boolean, default=True, nullable=False, comment="工单是否开启"
    )


class OjTicketAssigneeRel(Base):
    """工单处理人关系表。"""

    __tablename__ = "oj_ticket_assignee_rel"
    __table_args__ = (
        UniqueConstraint(
            "ticket_id", "account_id", name="uq_oj_ticket_assignee_rel_ticket_account"
        ),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    ticket_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="工单")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="处理人")


class OjTicketMessage(Base, TimestampMixin):
    """工单消息表。"""

    __tablename__ = "oj_ticket_message"
    __table_args__ = (Index("ix_oj_ticket_message_ticket", "ticket_id"),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    ticket_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="工单")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")
    body: Mapped[str] = mapped_column(Text, nullable=False, comment="消息内容")
    message_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, comment="消息时间"
    )
