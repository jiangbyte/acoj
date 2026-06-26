from sqlalchemy import String
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin


class PortalUserProfile(Base, TimestampMixin):
    """门户账户扩展资料表，仅保留 account_id 字段，不建立数据库外键。"""

    __tablename__ = "portal_user_profile"

    account_id: Mapped[str] = mapped_column(String(64), primary_key=True, comment="账户ID")
    nickname: Mapped[str | None] = mapped_column(String(64), comment="门户昵称")
    avatar_url: Mapped[str | None] = mapped_column(String(255), comment="门户头像地址")
    bio: Mapped[str | None] = mapped_column(String(255), comment="个人简介")
    level: Mapped[str | None] = mapped_column(String(32), comment="门户等级")
