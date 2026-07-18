"""OJ judge node model."""

from datetime import datetime

from sqlalchemy import JSON, Boolean, DateTime, Float, Index, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.oj.enums import (
    OjJudgeNodeStatus,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjJudgeNode(Base, TimestampMixin):
    """判题机节点。"""

    __tablename__ = "oj_judge_node"
    __table_args__ = (
        Index("ix_oj_judge_node_name", "name", unique=True),
        Index("ix_oj_judge_node_status_online", "status", "online"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    name: Mapped[str] = mapped_column(String(128), nullable=False, comment="判题机名称")
    auth_key_hash: Mapped[str] = mapped_column(String(255), nullable=False, comment="认证密钥哈希")
    status: Mapped[str] = mapped_column(
        String(32),
        default=OjJudgeNodeStatus.ENABLED.value,
        nullable=False,
        comment=f"状态：{OjJudgeNodeStatus.__doc__}",
    )
    online: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False, comment="是否在线")
    tier: Mapped[int] = mapped_column(Integer, default=1, nullable=False, comment="层级")
    last_ip: Mapped[str | None] = mapped_column(String(64), comment="最后连接IP")
    last_heartbeat_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), comment="最后心跳"
    )
    load: Mapped[float | None] = mapped_column(Float, comment="负载")
    supported_languages: Mapped[list[str]] = mapped_column(
        JSON, default=list, nullable=False, comment="支持语言ID"
    )
    supported_modes: Mapped[list[str]] = mapped_column(
        JSON, default=list, nullable=False, comment="支持判题模式"
    )
    description: Mapped[str | None] = mapped_column(Text, comment="描述")
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")
