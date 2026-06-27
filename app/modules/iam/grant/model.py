from datetime import datetime

from sqlalchemy import DateTime, JSON, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.core.config.enums import DataScope, GrantEffect, GrantMode, StatusEnum
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class SysSubjectResourceGrantRel(Base, TimestampMixin):
    """主体资源授权表，主体拿到资源后即继承资源下挂载的权限项。"""

    __tablename__ = "sys_subject_resource_grant_rel"
    __table_args__ = (
        UniqueConstraint("subject_type", "subject_id", "resource_id", name="uq_sys_subject_resource_grant_rel_subject_resource"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    subject_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="授权主体类型")
    subject_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="授权主体ID")
    resource_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="资源ID")
    grant_mode: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        default=GrantMode.CASCADE.value,
        comment="授权模式",
    )
    effect: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        default=GrantEffect.ALLOW.value,
        comment="授权效果",
    )
    status: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        default=StatusEnum.ENABLED.value,
        comment="状态",
    )
    description: Mapped[str | None] = mapped_column(Text, comment="描述")
    expired_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="失效时间")


class SysSubjectPermissionGrantRel(Base, TimestampMixin):
    """主体权限例外授权表，只承担用户或账户组的例外补权、限权和自定义数据范围。"""

    __tablename__ = "sys_subject_permission_grant_rel"
    __table_args__ = (
        UniqueConstraint("subject_type", "subject_id", "permission_key", name="uq_sys_subject_permission_grant_rel_subject_permission"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    subject_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="授权主体类型")
    subject_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="授权主体ID")
    permission_key: Mapped[str] = mapped_column(String(128), nullable=False, comment="权限标识")
    data_scope: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        default=DataScope.SELF.value,
        comment="数据范围",
    )
    custom_scope_dept_ids: Mapped[list[str]] = mapped_column(
        JSON,
        default=list,
        nullable=False,
        comment="自定义数据范围部门ID列表",
    )
    effect: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        default=GrantEffect.ALLOW.value,
        comment="授权效果",
    )
    status: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        default=StatusEnum.ENABLED.value,
        comment="状态",
    )
    description: Mapped[str | None] = mapped_column(Text, comment="描述")
    reason: Mapped[str | None] = mapped_column(Text, comment="授权原因")
    expired_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="失效时间")
