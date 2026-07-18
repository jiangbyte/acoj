"""OJ community favorite model."""

from sqlalchemy import Index, String
from sqlalchemy.orm import Mapped, mapped_column

from app.modules.oj.enums import (
    OjFavoriteTargetType,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjFavorite(Base, TimestampMixin):
    """收藏。"""

    __tablename__ = "oj_favorite"
    __table_args__ = (
        Index("ix_oj_favorite_account", "account_type", "account_id"),
        Index(
            "uq_oj_favorite_account",
            "target_type",
            "target_id",
            "account_type",
            "account_id",
            unique=True,
        ),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    target_type: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        comment=f"目标类型：{OjFavoriteTargetType.__doc__}",
    )
    target_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="目标ID")
    account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="账户类型")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
