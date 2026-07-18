"""OJ problem tag model."""

from sqlalchemy import Index, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.core.config.enums import StatusEnum
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjProblemTag(Base, TimestampMixin):
    """题目标签。"""

    __tablename__ = "oj_problem_tag"
    __table_args__ = (Index("ix_oj_problem_tag_code", "code", unique=True),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    code: Mapped[str] = mapped_column(String(64), nullable=False, comment="标签编码")
    name: Mapped[str] = mapped_column(String(128), nullable=False, comment="标签名称")
    color: Mapped[str | None] = mapped_column(String(32), comment="颜色")
    description: Mapped[str | None] = mapped_column(Text, comment="描述")
    status: Mapped[str] = mapped_column(
        String(32),
        default=StatusEnum.ENABLED.value,
        nullable=False,
        comment="状态",
    )
