from datetime import datetime

from sqlalchemy import Boolean, DateTime, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.core.config.enums import AccountStatusEnum
from app.modules.iam.enums import AccountIdentityBindStatus
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class SysAccount(Base, TimestampMixin):
    """系统账户主表，只负责账户主体、安全状态和审计信息。"""

    __tablename__ = "sys_account"

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    password_hash: Mapped[str] = mapped_column(String(255), nullable=False, comment="密码哈希")
    account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="账户类型")
    account_status: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        default=AccountStatusEnum.ENABLED.value,
        comment="账户状态",
    )
    cancelled_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="注销时间")
    cancelled_by: Mapped[str | None] = mapped_column(String(64), comment="注销人")
    cancel_reason: Mapped[str | None] = mapped_column(Text, comment="注销原因")
    last_login_ip: Mapped[str | None] = mapped_column(String(64), comment="上次登录IP")
    last_login_address: Mapped[str | None] = mapped_column(String(255), comment="上次登录地点")
    last_login_time: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="上次登录时间")
    last_login_device: Mapped[str | None] = mapped_column(Text, comment="上次登录设备")
    latest_login_ip: Mapped[str | None] = mapped_column(String(64), comment="最新登录IP")
    latest_login_address: Mapped[str | None] = mapped_column(String(255), comment="最新登录地点")
    latest_login_time: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="最新登录时间")
    latest_login_device: Mapped[str | None] = mapped_column(Text, comment="最新登录设备")


class SysAccountIdentity(Base, TimestampMixin):
    """账户登录标识表，承载账号名、邮箱、手机号等可认证入口。"""

    __tablename__ = "sys_account_identity"
    __table_args__ = (
        UniqueConstraint("identity_type", "identifier", name="uq_sys_account_identity_type_identifier"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    identity_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="登录标识类型")
    identifier: Mapped[str] = mapped_column(String(128), nullable=False, comment="登录标识")
    verified: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False, comment="是否已验证")
    is_primary: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False, comment="是否主标识")
    bind_status: Mapped[str] = mapped_column(
        String(32),
        default=AccountIdentityBindStatus.BOUND.value,
        nullable=False,
        comment="绑定状态",
    )


class SysAccountRoleRel(Base, TimestampMixin):
    """账户角色关系表，用于表达账户直接拥有的角色集合。"""

    __tablename__ = "sys_account_role_rel"
    __table_args__ = (UniqueConstraint("account_id", "role_id", name="uq_sys_account_role_rel_account_role"),)

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    role_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="角色ID")


class SysAccountDeptRel(Base, TimestampMixin):
    """账户部门关系表，明确账户属于哪些部门以及主部门归属。"""

    __tablename__ = "sys_account_dept_rel"
    __table_args__ = (UniqueConstraint("account_id", "dept_id", name="uq_sys_account_dept_rel_account_dept"),)

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    dept_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="部门ID")
    is_primary: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False, comment="是否主部门")


class SysAccountGroupRel(Base, TimestampMixin):
    """账户与账户组关系表，用于表达账户所属分组。"""

    __tablename__ = "sys_account_group_rel"
    __table_args__ = (UniqueConstraint("account_id", "group_id", name="uq_sys_account_group_rel_account_group"),)

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    group_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户组ID")
