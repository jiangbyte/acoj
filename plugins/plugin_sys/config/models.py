from typing import Optional
import datetime

from sqlalchemy import Index, text
from sqlalchemy.dialects.mysql import DATETIME, INTEGER, TEXT, VARCHAR
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    pass


class SysConfig(Base):
    __tablename__ = "sys_config"
    __table_args__ = (
        Index("idx_category", "category"),
        {"comment": "系统配置"},
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), primary_key=True, comment="主键")
    config_key: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset="utf8mb4", collation="utf8mb4_general_ci"), comment="配置键")
    config_value: Mapped[Optional[str]] = mapped_column(TEXT(collation="utf8mb4_general_ci"), comment="配置值")
    category: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset="utf8mb4", collation="utf8mb4_general_ci"), comment="分类")
    remark: Mapped[Optional[str]] = mapped_column(VARCHAR(500, charset="utf8mb4", collation="utf8mb4_general_ci"), comment="备注")
    sort_code: Mapped[Optional[int]] = mapped_column(INTEGER, default=0, server_default=text("0"), comment="排序码")
    extra: Mapped[Optional[str]] = mapped_column(TEXT(collation="utf8mb4_general_ci"), comment="扩展信息")
    created_at: Mapped[Optional[datetime.datetime]] = mapped_column(DATETIME, comment="创建时间")
    created_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), comment="创建用户")
    updated_at: Mapped[Optional[datetime.datetime]] = mapped_column(DATETIME, comment="修改时间")
    updated_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), comment="修改用户")
