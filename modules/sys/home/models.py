from typing import Optional
from sqlalchemy import Integer, DateTime
from sqlalchemy.dialects.mysql import VARCHAR
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    pass


class SysQuickAction(Base):
    __tablename__ = "sys_quick_action"
    __table_args__ = {"comment": "用户快捷方式"}

    id: Mapped[str] = mapped_column(
        VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), primary_key=True, comment="主键"
    )
    user_id: Mapped[str] = mapped_column(
        VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), comment="用户ID"
    )
    resource_id: Mapped[str] = mapped_column(
        VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), comment="资源ID"
    )
    sort_code: Mapped[Optional[int]] = mapped_column(Integer, default=0, comment="排序")
    is_deleted: Mapped[Optional[str]] = mapped_column(
        VARCHAR(8, charset="utf8mb4", collation="utf8mb4_general_ci"), default="NO", comment="逻辑删除"
    )
    created_at: Mapped[Optional[DateTime]] = mapped_column(DateTime, comment="创建时间")
    created_by: Mapped[Optional[str]] = mapped_column(
        VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), comment="创建用户"
    )
    updated_at: Mapped[Optional[DateTime]] = mapped_column(DateTime, comment="更新时间")
    updated_by: Mapped[Optional[str]] = mapped_column(
        VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), comment="更新用户"
    )
