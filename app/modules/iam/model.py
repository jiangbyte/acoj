from datetime import datetime

from sqlalchemy import JSON, Boolean, DateTime, Integer, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.core.config.enums import (
    AccountStatusEnum,
    DataScope,
    GrantEffect,
    GrantMode,
    RoleScopeType,
    StatusEnum,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class SysAccount(Base, TimestampMixin):
    """系统账户主表，只负责账户身份、登录信息、状态和审计信息。"""

    __tablename__ = "sys_account"
    __table_args__ = (UniqueConstraint("account", name="uq_sys_account_account"),)

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    account: Mapped[str] = mapped_column(String(64), nullable=False, comment="账号")
    password_hash: Mapped[str] = mapped_column(String(255), nullable=False, comment="密码哈希")
    account_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="账户类型")
    account_status: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        default=AccountStatusEnum.ENABLED.value,
        comment="账户状态",
    )
    name: Mapped[str] = mapped_column(String(64), nullable=False, comment="姓名")
    nickname: Mapped[str | None] = mapped_column(String(64), comment="昵称")
    avatar: Mapped[str | None] = mapped_column(Text, comment="头像")
    signature: Mapped[str | None] = mapped_column(Text, comment="个性签名")
    phone: Mapped[str | None] = mapped_column(String(32), comment="手机号")
    email: Mapped[str | None] = mapped_column(String(128), comment="邮箱")
    is_superuser: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False, comment="是否超级管理员")
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


class SysDept(Base, TimestampMixin):
    """部门表，只承担组织归属、层级关系和部门管理元信息。"""

    __tablename__ = "sys_dept"
    __table_args__ = (UniqueConstraint("code", name="uq_sys_dept_code"),)

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    parent_id: Mapped[str | None] = mapped_column(String(64), comment="父部门ID")
    master_id: Mapped[str | None] = mapped_column(String(64), comment="主管ID")
    deputy_master_id: Mapped[str | None] = mapped_column(String(64), comment="副主管ID")
    name: Mapped[str] = mapped_column(String(64), nullable=False, comment="部门名称")
    code: Mapped[str] = mapped_column(String(64), nullable=False, comment="部门编码")
    category: Mapped[str] = mapped_column(String(64), nullable=False, comment="部门类别")
    sort: Mapped[int] = mapped_column(Integer, default=99, nullable=False, comment="排序")
    is_virtual: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False, comment="是否虚拟部门")
    status: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        default=StatusEnum.ENABLED.value,
        comment="状态",
    )
    extra: Mapped[dict] = mapped_column(JSON, default=dict, comment="扩展信息")


class SysGroup(Base, TimestampMixin):
    """账户组表，作为批量授权、批量分组和批量例外授权的载体。"""

    __tablename__ = "sys_group"
    __table_args__ = (UniqueConstraint("name", name="uq_sys_group_name"),)

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    name: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户组名称")
    description: Mapped[str | None] = mapped_column(Text, comment="描述")
    status: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        default=StatusEnum.ENABLED.value,
        comment="状态",
    )
    extra: Mapped[dict] = mapped_column(JSON, default=dict, comment="扩展信息")


class SysPosition(Base, TimestampMixin):
    """职位表，用于描述岗位体系，本身不直接承担授权关系。"""

    __tablename__ = "sys_position"
    __table_args__ = (UniqueConstraint("code", name="uq_sys_position_code"),)

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    name: Mapped[str] = mapped_column(String(64), nullable=False, comment="职位名称")
    code: Mapped[str] = mapped_column(String(64), nullable=False, comment="职位编码")
    category: Mapped[str] = mapped_column(String(32), nullable=False, comment="职位类别")
    sort: Mapped[int] = mapped_column(Integer, default=99, nullable=False, comment="排序")
    is_virtual: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False, comment="是否虚拟职位")
    status: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        default=StatusEnum.ENABLED.value,
        comment="状态",
    )
    description: Mapped[str | None] = mapped_column(Text, comment="职位描述")
    extra: Mapped[dict] = mapped_column(JSON, default=dict, comment="扩展信息")


class SysRole(Base, TimestampMixin):
    """角色表，作为授权主载体，通过资源授权间接获得权限和数据范围。"""

    __tablename__ = "sys_role"
    __table_args__ = (UniqueConstraint("code", name="uq_sys_role_code"),)

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    code: Mapped[str] = mapped_column(String(64), nullable=False, comment="角色编码")
    name: Mapped[str] = mapped_column(String(64), nullable=False, comment="角色名称")
    category: Mapped[str] = mapped_column(String(64), nullable=False, comment="角色分类")
    scope_type: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        default=RoleScopeType.PLATFORM.value,
        comment="角色作用域类型",
    )
    owner_dept_id: Mapped[str | None] = mapped_column(String(64), comment="所属部门ID")
    sort: Mapped[int] = mapped_column(Integer, default=99, nullable=False, comment="排序")
    status: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        default=StatusEnum.ENABLED.value,
        comment="状态",
    )
    is_builtin: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False, comment="是否内置角色")
    description: Mapped[str | None] = mapped_column(Text, comment="描述")
    extra: Mapped[dict] = mapped_column(JSON, default=dict, comment="扩展信息")


class SysResource(Base, TimestampMixin):
    """资源表，统一描述目录、菜单、页面、按钮和接口分组等可授权资源节点。"""

    __tablename__ = "sys_resource"
    __table_args__ = (UniqueConstraint("code", name="uq_sys_resource_code"),)

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    parent_id: Mapped[str | None] = mapped_column(String(64), comment="父资源ID")
    code: Mapped[str] = mapped_column(String(64), nullable=False, comment="资源编码")
    name: Mapped[str] = mapped_column(String(64), nullable=False, comment="资源名称")
    resource_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="资源类型")
    module: Mapped[str | None] = mapped_column(String(64), comment="所属模块")
    path: Mapped[str | None] = mapped_column(String(255), comment="路由路径")
    component: Mapped[str | None] = mapped_column(String(255), comment="前端组件")
    redirect: Mapped[str | None] = mapped_column(String(255), comment="重定向地址")
    icon: Mapped[str | None] = mapped_column(String(255), comment="图标")
    href: Mapped[str | None] = mapped_column(String(255), comment="外链地址")
    sort: Mapped[int] = mapped_column(Integer, default=99, nullable=False, comment="排序")
    is_visible: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False, comment="是否可见")
    is_cache: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False, comment="是否缓存")
    is_affix: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False, comment="是否固定标签")
    status: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        default=StatusEnum.ENABLED.value,
        comment="状态",
    )
    description: Mapped[str | None] = mapped_column(Text, comment="描述")
    extra: Mapped[dict] = mapped_column(JSON, default=dict, comment="扩展信息")


class SysResourcePermissionRel(Base, TimestampMixin):
    """资源权限项关系表，用于描述资源节点下挂载的权限及其数据范围。"""

    __tablename__ = "sys_resource_permission_rel"
    __table_args__ = (
        UniqueConstraint("resource_id", "permission_key", name="uq_sys_resource_permission_rel_resource_permission"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    resource_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="资源ID")
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
    sort: Mapped[int] = mapped_column(Integer, default=99, nullable=False, comment="排序")
    status: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        default=StatusEnum.ENABLED.value,
        comment="状态",
    )
    description: Mapped[str | None] = mapped_column(Text, comment="描述")


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


class SysGroupRoleRel(Base, TimestampMixin):
    """账户组角色关系表，用于批量把角色赋予到账户组。"""

    __tablename__ = "sys_group_role_rel"
    __table_args__ = (UniqueConstraint("group_id", "role_id", name="uq_sys_group_role_rel_group_role"),)

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    group_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户组ID")
    role_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="角色ID")
