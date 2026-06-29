from sqlalchemy import Boolean, Integer, JSON, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.core.config.enums import DataScope, StatusEnum
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class SysResource(Base, TimestampMixin):
    """资源表，统一描述目录、菜单、页面、按钮和接口分组等可授权资源节点。"""

    __tablename__ = "sys_resource"
    __table_args__ = (UniqueConstraint("code", name="uq_sys_resource_code"),)

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    parent_id: Mapped[str | None] = mapped_column(String(64), comment="父资源ID")
    code: Mapped[str] = mapped_column(String(64), nullable=False, comment="资源编码")
    name: Mapped[str] = mapped_column(String(64), nullable=False, comment="资源名称")
    resource_type: Mapped[str] = mapped_column(String(32), nullable=False, comment="资源类型")
    module_id: Mapped[str | None] = mapped_column(String(64), comment="所属资源模块ID")
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


class SysResourceModule(Base, TimestampMixin):
    """资源模块表，用于资源管理和授权页面的模块分组。"""

    __tablename__ = "sys_resource_module"
    __table_args__ = (UniqueConstraint("code", name="uq_sys_resource_module_code"),)

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id, comment="主键")
    name: Mapped[str] = mapped_column(String(64), nullable=False, comment="模块名称")
    code: Mapped[str] = mapped_column(String(64), nullable=False, comment="模块编码")
    icon: Mapped[str | None] = mapped_column(String(255), comment="图标")
    color: Mapped[str | None] = mapped_column(String(32), comment="颜色")
    sort: Mapped[int] = mapped_column(Integer, default=99, nullable=False, comment="排序")
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
