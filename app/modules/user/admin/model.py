from sqlalchemy import String
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin


class AdminUserProfile(Base, TimestampMixin):
    """管理端账户扩展资料表，仅通过 account_id 与账户主表在业务层关联。"""

    __tablename__ = "admin_user_profile"

    account_id: Mapped[str] = mapped_column(String(64), primary_key=True, comment="账户ID")
    real_name: Mapped[str | None] = mapped_column(String(64), comment="真实姓名")
    avatar_url: Mapped[str | None] = mapped_column(String(255), comment="头像地址")
    title: Mapped[str | None] = mapped_column(String(64), comment="岗位头衔")
    employee_no: Mapped[str | None] = mapped_column(String(64), comment="员工编号")
