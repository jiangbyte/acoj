"""SysFile ORM model — mirrors hei-gin plugins/plugin-sys/file/model.go."""

from __future__ import annotations

from datetime import datetime
from typing import Optional
from sqlalchemy import Index, text
from sqlalchemy.dialects.mysql import BIGINT, BOOLEAN, DATETIME, TEXT, VARCHAR
from sqlalchemy.orm import Mapped, mapped_column
from sdk.kernel.registry import HeiBase


class SysFile(HeiBase):
    """系统文件存储记录（与IM文件共用存储引擎，但独立表）"""
    __tablename__ = "sys_file"
    __table_args__ = (
        Index("idx_file_key", "file_key", unique=True),
        {"comment": "系统文件"},
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), primary_key=True)
    engine: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    bucket: Mapped[str] = mapped_column(VARCHAR(128, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    file_key: Mapped[str] = mapped_column(VARCHAR(500, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    name: Mapped[str] = mapped_column(VARCHAR(255, charset="utf8mb4", collation="utf8mb4_general_ci"), nullable=False)
    suffix: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"))
    size_kb: Mapped[int] = mapped_column(BIGINT, default=0, server_default=text("0"))
    size_info: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"))
    obj_name: Mapped[Optional[str]] = mapped_column(VARCHAR(500, charset="utf8mb4", collation="utf8mb4_general_ci"))
    storage_path: Mapped[Optional[str]] = mapped_column(VARCHAR(500, charset="utf8mb4", collation="utf8mb4_general_ci"))
    download_path: Mapped[Optional[str]] = mapped_column(VARCHAR(500, charset="utf8mb4", collation="utf8mb4_general_ci"))
    is_download_auth: Mapped[bool] = mapped_column(BOOLEAN, default=False, server_default=text("false"))
    thumbnail: Mapped[Optional[str]] = mapped_column(VARCHAR(500, charset="utf8mb4", collation="utf8mb4_general_ci"))
    checksum: Mapped[Optional[str]] = mapped_column(VARCHAR(128, charset="utf8mb4", collation="utf8mb4_general_ci"))
    checksum_algo: Mapped[Optional[str]] = mapped_column(VARCHAR(16, charset="utf8mb4", collation="utf8mb4_general_ci"))
    ext_json: Mapped[Optional[str]] = mapped_column(TEXT(collation="utf8mb4_general_ci"))
    created_at: Mapped[datetime] = mapped_column(DATETIME)
    created_by: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"))
    updated_at: Mapped[datetime] = mapped_column(DATETIME)
    updated_by: Mapped[str] = mapped_column(VARCHAR(32, charset="utf8mb4", collation="utf8mb4_general_ci"))
