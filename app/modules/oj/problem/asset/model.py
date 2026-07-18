"""OJ problem asset model."""

from sqlalchemy import JSON, BigInteger, Index, String
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjProblemAsset(Base, TimestampMixin):
    """题目附件、数据包和辅助文件。"""

    __tablename__ = "oj_problem_asset"
    __table_args__ = (Index("ix_oj_problem_asset_problem_type", "problem_id", "asset_type"),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目ID")
    asset_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="资源类型")
    name: Mapped[str] = mapped_column(String(255), nullable=False, comment="名称")
    url: Mapped[str | None] = mapped_column(String(1024), comment="访问地址")
    storage_key: Mapped[str | None] = mapped_column(String(1024), comment="存储键")
    checksum: Mapped[str | None] = mapped_column(String(128), comment="校验值")
    size: Mapped[int | None] = mapped_column(BigInteger, comment="大小")
    version: Mapped[str | None] = mapped_column(String(64), comment="版本")
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")
