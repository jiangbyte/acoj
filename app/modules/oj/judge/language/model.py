"""OJ judge language model."""

from sqlalchemy import JSON, Index, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.core.config.enums import StatusEnum
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjLanguage(Base, TimestampMixin):
    """提交语言配置。"""

    __tablename__ = "oj_language"
    __table_args__ = (Index("ix_oj_language_key", "key", unique=True),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    key: Mapped[str] = mapped_column(String(32), nullable=False, comment="语言标识")
    name: Mapped[str] = mapped_column(String(64), nullable=False, comment="语言名称")
    short_name: Mapped[str | None] = mapped_column(String(32), comment="短名称")
    common_name: Mapped[str | None] = mapped_column(String(32), comment="通用名称")
    ace_mode: Mapped[str | None] = mapped_column(String(64), comment="Ace 模式")
    pygments: Mapped[str | None] = mapped_column(String(64), comment="Pygments 名称")
    extension: Mapped[str | None] = mapped_column(String(32), comment="文件扩展名")
    template: Mapped[str | None] = mapped_column(Text, comment="代码模板")
    compile_command: Mapped[str | None] = mapped_column(Text, comment="编译命令")
    run_command: Mapped[str | None] = mapped_column(Text, comment="运行命令")
    status: Mapped[str] = mapped_column(
        String(32),
        default=StatusEnum.ENABLED.value,
        nullable=False,
        comment="状态",
    )
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")
