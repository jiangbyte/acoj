"""OJ license model — 题目授权协议。"""

from sqlalchemy import Index, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.core.config.enums import StatusEnum
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjLicense(Base, TimestampMixin):
    """题目授权协议（如 CC BY-SA 4.0）。"""

    __tablename__ = "oj_license"
    __table_args__ = (Index("ix_oj_license_key", "key", unique=True),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    key: Mapped[str] = mapped_column(String(64), nullable=False, comment="协议标识")
    name: Mapped[str] = mapped_column(String(128), nullable=False, comment="协议名称")
    display: Mapped[str | None] = mapped_column(String(256), comment="显示名称")
    url: Mapped[str | None] = mapped_column(String(512), comment="协议链接")
    icon: Mapped[str | None] = mapped_column(String(512), comment="协议图标")
    text: Mapped[str | None] = mapped_column(Text, comment="协议全文")
    status: Mapped[str] = mapped_column(
        String(32),
        default=StatusEnum.ENABLED.value,
        nullable=False,
        comment="状态",
    )
