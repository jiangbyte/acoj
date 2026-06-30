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


class MsgGroup(Base, TimestampMixin):
    """消息群组，与 IAM 用户组无关。"""

    __tablename__ = "msg_group"

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    name: Mapped[str] = mapped_column(String(128), nullable=False, comment="群组名称")
    owner_account_type: Mapped[str | None] = mapped_column(String(32), comment="群主账户类型")
    owner_account_id: Mapped[str | None] = mapped_column(String(64), comment="群主账户ID")
    avatar: Mapped[str | None] = mapped_column(String(500), comment="群头像")
    status: Mapped[str] = mapped_column(
        String(32),
        default=MessageGroupStatus.ENABLED.value,
        nullable=False,
        comment="状态",
    )
    description: Mapped[str | None] = mapped_column(Text, comment="描述")
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")


class MsgGroupMember(Base):
    """消息群组成员。"""

    __tablename__ = "msg_group_member"
    __table_args__ = (
        UniqueConstraint("group_id", "account_type", "account_id", name="uq_msg_group_member_account"),
        Index("ix_msg_group_member_account", "account_type", "account_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    group_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="群组ID")
    account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="账户类型")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    nickname: Mapped[str | None] = mapped_column(String(64), comment="群昵称")
    is_muted: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False, comment="是否免打扰")
    joined_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, comment="加入时间")
    left_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="退出时间")


class MsgThread(Base, TimestampMixin):
    """消息会话。"""

    __tablename__ = "msg_thread"
    __table_args__ = (
        Index("ix_msg_thread_type_status_last", "thread_type", "status", "last_message_at"),
        Index("ix_msg_thread_group", "group_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    thread_type: Mapped[str] = mapped_column(
        String(32),
        default=MessageThreadType.DIRECT.value,
        nullable=False,
        comment="会话类型",
    )
    title: Mapped[str | None] = mapped_column(String(255), comment="会话标题")
    group_id: Mapped[str | None] = mapped_column(String(64), comment="消息群组ID")
    created_account_type: Mapped[str | None] = mapped_column(String(32), comment="创建账户类型")
    created_account_id: Mapped[str | None] = mapped_column(String(64), comment="创建账户ID")
    status: Mapped[str] = mapped_column(
        String(32),
        default=MessageThreadStatus.ACTIVE.value,
        nullable=False,
        comment="状态",
    )
    last_message_id: Mapped[str | None] = mapped_column(String(64), comment="最后消息ID")
    last_message_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="最后消息时间")
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")


class MsgThreadParticipant(Base):
    """会话参与者，聚合未读数量，列表查询不需要逐消息扫描。"""

    __tablename__ = "msg_thread_participant"
    __table_args__ = (
        UniqueConstraint("thread_id", "account_type", "account_id", name="uq_msg_thread_participant_account"),
        Index("ix_msg_thread_participant_account", "account_type", "account_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    thread_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="会话ID")
    account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="账户类型")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    unread_count: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="未读数")
    last_read_message_id: Mapped[str | None] = mapped_column(String(64), comment="最后已读消息ID")
    last_read_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="最后阅读时间")
    is_muted: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False, comment="是否免打扰")
    joined_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, comment="加入时间")
    left_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="退出时间")


class MsgMessage(Base, TimestampMixin):
    """会话消息，parent_id 用于回复。"""

    __tablename__ = "msg_message"
    __table_args__ = (
        Index("ix_msg_message_thread_created", "thread_id", "created_at"),
        Index("ix_msg_message_parent", "parent_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    thread_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="会话ID")
    parent_id: Mapped[str | None] = mapped_column(String(64), comment="回复消息ID")
    sender_type: Mapped[str] = mapped_column(
        String(32),
        default=MessageSenderType.USER.value,
        nullable=False,
        comment="发送方类型",
    )
    sender_account_type: Mapped[str | None] = mapped_column(String(32), comment="发送账户类型")
    sender_account_id: Mapped[str | None] = mapped_column(String(64), comment="发送账户ID")
    sender_name: Mapped[str | None] = mapped_column(String(128), comment="发送方快照名称")
    content: Mapped[str] = mapped_column(Text, nullable=False, comment="内容")
    content_type: Mapped[str] = mapped_column(
        String(32),
        default=MessageContentType.TEXT.value,
        nullable=False,
        comment="内容格式",
    )
    reply_count: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="回复数")
    is_revoked: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False, comment="是否撤回")
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")


class MsgMessageReceipt(Base):
    """消息已读回执，保留精确已读扩展空间。"""

    __tablename__ = "msg_message_receipt"
    __table_args__ = (
        UniqueConstraint("message_id", "account_type", "account_id", name="uq_msg_message_receipt_account"),
        Index("ix_msg_message_receipt_account", "account_type", "account_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    message_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="消息ID")
    thread_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="会话ID")
    account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="账户类型")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    read_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="阅读时间")


class MsgMessageAttachment(Base):
    """消息附件预留表。"""

    __tablename__ = "msg_message_attachment"
    __table_args__ = (Index("ix_msg_message_attachment_message", "message_id", "sort"),)

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    message_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="消息ID")
    name: Mapped[str] = mapped_column(String(255), nullable=False, comment="文件名")
    url: Mapped[str] = mapped_column(String(1024), nullable=False, comment="文件地址")
    content_type: Mapped[str | None] = mapped_column(String(128), comment="文件类型")
    size: Mapped[int | None] = mapped_column(BigInteger, comment="文件大小")
    sort: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="排序")
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")


class MsgMessageReaction(Base):
    """消息表情反应预留表。"""

    __tablename__ = "msg_message_reaction"
    __table_args__ = (
        UniqueConstraint("message_id", "account_type", "account_id", "reaction", name="uq_msg_message_reaction_account"),
        Index("ix_msg_message_reaction_message", "message_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    message_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="消息ID")
    account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="账户类型")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    reaction: Mapped[str] = mapped_column(String(64), nullable=False, comment="反应")
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, comment="创建时间")
