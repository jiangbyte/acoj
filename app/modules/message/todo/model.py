from datetime import datetime

from sqlalchemy import BigInteger, Boolean, DateTime, Index, Integer, JSON, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.message.enums import (
    MessageContentType,
    MessageGroupStatus,
    MessageSenderType,
    MessageTargetScope,
    MessageThreadStatus,
    MessageThreadType,
    NotificationSeverity,
    NotificationStatus,
    TodoAssigneeStatus,
    TodoPriority,
    TodoStatus,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class MsgTodo(Base, TimestampMixin):
    """待办任务，支持全局和指定账户分派。"""

    __tablename__ = "msg_todo"
    __table_args__ = (
        Index("ix_msg_todo_status_scope_due", "status", "target_scope", "due_at"),
        Index("ix_msg_todo_target_account", "target_account_type", "target_account_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    title: Mapped[str] = mapped_column(String(255), nullable=False, comment="标题")
    content: Mapped[str | None] = mapped_column(Text, comment="内容")
    content_type: Mapped[str] = mapped_column(
        String(32),
        default=MessageContentType.TEXT.value,
        nullable=False,
        comment="内容格式",
    )
    priority: Mapped[str] = mapped_column(
        String(32),
        default=TodoPriority.NORMAL.value,
        nullable=False,
        comment="优先级",
    )
    target_scope: Mapped[str] = mapped_column(
        String(32),
        default=MessageTargetScope.SPECIFIC.value,
        nullable=False,
        comment="目标范围",
    )
    target_account_type: Mapped[str | None] = mapped_column(String(32), comment="目标账户类型")
    target_account_id: Mapped[str | None] = mapped_column(String(64), comment="目标账户ID")
    creator_account_type: Mapped[str | None] = mapped_column(String(32), comment="创建账户类型")
    creator_account_id: Mapped[str | None] = mapped_column(String(64), comment="创建账户ID")
    source_type: Mapped[str | None] = mapped_column(String(64), comment="来源类型")
    source_id: Mapped[str | None] = mapped_column(String(64), comment="来源ID")
    status: Mapped[str] = mapped_column(String(32), default=TodoStatus.PENDING.value, nullable=False, comment="状态")
    due_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="截止时间")
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")


class MsgTodoAssignee(Base):
    """待办账户状态。"""

    __tablename__ = "msg_todo_assignee"
    __table_args__ = (
        UniqueConstraint("todo_id", "account_type", "account_id", name="uq_msg_todo_assignee_account"),
        Index("ix_msg_todo_assignee_account", "account_type", "account_id", "status"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    todo_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="待办ID")
    account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="账户类型")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    status: Mapped[str] = mapped_column(
        String(32),
        default=TodoAssigneeStatus.PENDING.value,
        nullable=False,
        comment="处理状态",
    )
    read_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="阅读时间")
    started_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="开始时间")
    completed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="完成时间")
    cancelled_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="取消时间")
