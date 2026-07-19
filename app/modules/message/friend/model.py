from datetime import datetime

from sqlalchemy import DateTime, Index, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.message.enums import FriendRequestStatus, FriendStatus
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class MsgFriend(Base, TimestampMixin):
    """好友关系表，双向记录。A 添加 B 为好友时写入 (A,B) 和 (B,A) 两条记录。"""

    __tablename__ = "msg_friend"
    __table_args__ = (
        UniqueConstraint(
            "account_type", "account_id", "friend_account_type", "friend_account_id",
            name="uq_msg_friend_pair",
        ),
        Index("ix_msg_friend_account", "account_type", "account_id"),
        Index("ix_msg_friend_friend", "friend_account_type", "friend_account_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="账户类型")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    friend_account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="好友账户类型")
    friend_account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="好友账户ID")
    remark: Mapped[str | None] = mapped_column(String(64), comment="备注名")
    status: Mapped[str] = mapped_column(
        String(32), default=FriendStatus.ACTIVE.value, nullable=False, comment="状态",
    )
    friend_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, comment="成为好友时间",
    )


class MsgFriendRequest(Base, TimestampMixin):
    """好友申请表。"""

    __tablename__ = "msg_friend_request"
    __table_args__ = (
        UniqueConstraint(
            "applicant_type", "applicant_id", "recipient_type", "recipient_id",
            name="uq_msg_friend_request_pair",
        ),
        Index("ix_msg_friend_request_recipient", "recipient_type", "recipient_id", "status"),
        Index("ix_msg_friend_request_applicant", "applicant_type", "applicant_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    applicant_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="申请人账户类型")
    applicant_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="申请人账户ID")
    recipient_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="接收人账户类型")
    recipient_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="接收人账户ID")
    message: Mapped[str | None] = mapped_column(Text, comment="申请附言")
    status: Mapped[str] = mapped_column(
        String(32), default=FriendRequestStatus.PENDING.value, nullable=False, comment="状态",
    )
    handled_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="处理时间")
