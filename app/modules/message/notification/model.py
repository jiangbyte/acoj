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


class MsgNotification(Base, TimestampMixin):
    """通知，支持全局、端范围和指定账户投递。"""

    __tablename__ = "msg_notification"
    __table_args__ = (
        Index("ix_msg_notification_status_scope_publish", "status", "target_scope", "publish_at"),
        Index("ix_msg_notification_target_account", "target_account_type", "target_account_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    title: Mapped[str] = mapped_column(String(255), nullable=False, comment="标题")
    content: Mapped[str] = mapped_column(Text, nullable=False, comment="内容")
    content_type: Mapped[str] = mapped_column(
        String(32),
        default=MessageContentType.TEXT.value,
        nullable=False,
        comment="内容格式",
    )
    severity: Mapped[str] = mapped_column(
        String(32),
        default=NotificationSeverity.INFO.value,
        nullable=False,
        comment="等级",
    )
    target_scope: Mapped[str] = mapped_column(
        String(32),
        default=MessageTargetScope.ALL.value,
        nullable=False,
        comment="目标范围",
    )
    target_account_type: Mapped[str | None] = mapped_column(String(32), comment="目标账户类型")
    target_account_id: Mapped[str | None] = mapped_column(String(64), comment="目标账户ID")
    sender_account_type: Mapped[str | None] = mapped_column(String(32), comment="发送账户类型")
    sender_account_id: Mapped[str | None] = mapped_column(String(64), comment="发送账户ID")
    status: Mapped[str] = mapped_column(
        String(32),
        default=NotificationStatus.DRAFT.value,
        nullable=False,
        comment="状态",
    )
    publish_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="发布时间")
    revoked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="撤回时间")
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")


class MsgNotificationRead(Base):
    """通知已读状态，按账户维度记录。"""

    __tablename__ = "msg_notification_read"
    __table_args__ = (
        UniqueConstraint("notification_id", "account_type", "account_id", name="uq_msg_notification_read_account"),
        Index("ix_msg_notification_read_account", "account_type", "account_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    notification_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="通知ID")
    account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="账户类型")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    read_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, comment="阅读时间")
