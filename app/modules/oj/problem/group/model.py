"""OJ problem group model — 树形题目分组。"""

from sqlalchemy import Index, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.core.config.enums import StatusEnum
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjProblemGroup(Base, TimestampMixin):
    """题目分组，支持通过 parent_id 自引用构造树形层级。"""

    __tablename__ = "oj_problem_group"
    __table_args__ = (
        Index("ix_oj_problem_group_code", "code", unique=True),
        Index("ix_oj_problem_group_parent", "parent_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    parent_id: Mapped[str | None] = mapped_column(String(64), comment="父分组ID")
    code: Mapped[str] = mapped_column(String(64), nullable=False, comment="分组编码")
    name: Mapped[str] = mapped_column(String(128), nullable=False, comment="分组名称")
    full_name: Mapped[str | None] = mapped_column(String(255), comment="完整名称")
    description: Mapped[str | None] = mapped_column(Text, comment="描述")
    icon: Mapped[str | None] = mapped_column(String(256), comment="图标")
    color: Mapped[str | None] = mapped_column(String(32), comment="颜色")
    sort: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="排序")
    status: Mapped[str] = mapped_column(
        String(32),
        default=StatusEnum.ENABLED.value,
        nullable=False,
        comment="状态",
    )
