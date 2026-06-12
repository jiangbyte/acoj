"""Home module models — mirrors hei-gin plugin-sys/home/model.go."""

from __future__ import annotations

import datetime
from typing import Optional
from sqlalchemy import UniqueConstraint, text
from sqlalchemy.dialects.mysql import DATETIME, INTEGER, VARCHAR
from sqlalchemy.orm import Mapped, mapped_column
from core.plugin.registry import HeiBase


class SysQuickAction(HeiBase):
    __tablename__ = "sys_quick_action"
    __table_args__ = (
        UniqueConstraint("user_id", "resource_id", name="idx_user_resource"),
        {"comment": "用户快捷方式"},
    )

    id: Mapped[str] = mapped_column(
        VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), primary_key=True, comment="主键"
    )
    user_id: Mapped[str] = mapped_column(
        VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), comment="用户ID"
    )
    resource_id: Mapped[str] = mapped_column(
        VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), comment="资源ID"
    )
    sort_code: Mapped[Optional[int]] = mapped_column(INTEGER, default=0, server_default=text("0"), comment="排序")
    created_at: Mapped[Optional[datetime.datetime]] = mapped_column(DATETIME, comment="创建时间")
    created_by: Mapped[Optional[str]] = mapped_column(
        VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), comment="创建用户"
    )
    updated_at: Mapped[Optional[datetime.datetime]] = mapped_column(DATETIME, comment="更新时间")
    updated_by: Mapped[Optional[str]] = mapped_column(
        VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), comment="更新用户"
    )
