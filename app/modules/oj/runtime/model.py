from datetime import datetime

from sqlalchemy import Boolean, DateTime, Float, Index, Integer, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjLanguage(Base, TimestampMixin):
    """编程语言表。"""

    __tablename__ = "oj_language"
    __table_args__ = (
        UniqueConstraint("key", name="uq_oj_language_key"),
        Index("ix_oj_language_common_name", "common_name"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    key: Mapped[str] = mapped_column(String(6), nullable=False, comment="短标识")
    name: Mapped[str] = mapped_column(String(20), nullable=False, comment="完整名称")
    short_name: Mapped[str | None] = mapped_column(String(10), comment="简称")
    common_name: Mapped[str] = mapped_column(String(10), nullable=False, comment="通用名称")
    ace: Mapped[str] = mapped_column(String(20), nullable=False, comment="Ace 模式名称")
    pygments: Mapped[str] = mapped_column(String(20), nullable=False, comment="Pygments 名称")
    template: Mapped[str] = mapped_column(Text, default="", nullable=False, comment="代码模板")
    runtime_information: Mapped[str] = mapped_column(
        String(50), default="", nullable=False, comment="运行时信息覆盖值"
    )
    description: Mapped[str] = mapped_column(
        Text, default="", nullable=False, comment="语言描述"
    )
    extension: Mapped[str] = mapped_column(String(10), nullable=False, comment="文件扩展名")


class OjRuntimeVersion(Base, TimestampMixin):
    """运行时版本表。"""

    __tablename__ = "oj_runtime_version"
    __table_args__ = (
        UniqueConstraint("language_id", "name", name="uq_oj_runtime_version_language_name"),
        Index("ix_oj_runtime_version_language_priority", "language_id", "priority"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    language_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="语言")
    name: Mapped[str] = mapped_column(String(64), nullable=False, comment="运行时名称")
    version: Mapped[str] = mapped_column(
        String(64), default="", nullable=False, comment="运行时版本"
    )
    priority: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="优先级")


class OjJudge(Base, TimestampMixin):
    """评测机表。"""

    __tablename__ = "oj_judge"
    __table_args__ = (UniqueConstraint("name", name="uq_oj_judge_name"),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    name: Mapped[str] = mapped_column(String(50), nullable=False, comment="评测机名称")
    registered_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, comment="创建时间"
    )
    authentication_key: Mapped[str] = mapped_column(String(100), nullable=False, comment="认证密钥")
    is_blocked: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否阻塞评测机"
    )
    is_disabled: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否禁用评测机"
    )
    tier: Mapped[int] = mapped_column(Integer, default=1, nullable=False, comment="评测机层级")
    online: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="在线状态"
    )
    started_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), comment="评测机启动时间"
    )
    ping: Mapped[float | None] = mapped_column(Float, comment="响应时间")
    load: Mapped[float | None] = mapped_column(Float, comment="系统负载")
    description: Mapped[str] = mapped_column(
        Text, default="", nullable=False, comment="描述"
    )
    last_ip_address: Mapped[str | None] = mapped_column(String(64), comment="最近连接 IP")
