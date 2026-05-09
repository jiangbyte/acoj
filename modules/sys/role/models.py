from typing import Optional
import datetime

from sqlalchemy import DateTime, Index, Integer, Text, text
from sqlalchemy.dialects.mysql import VARCHAR
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column

class Base(DeclarativeBase):
    pass


class SysRole(Base):
    __tablename__ = 'sys_role'
    __table_args__ = (
        Index('uk_code', 'code', unique=True),
        {'comment': '角色'}
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), primary_key=True, comment='主键')
    code: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='角色编码')
    name: Mapped[str] = mapped_column(VARCHAR(64, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='角色名称')
    category: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='角色类别')
    description: Mapped[Optional[str]] = mapped_column(VARCHAR(500, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='角色描述')
    status: Mapped[Optional[str]] = mapped_column(VARCHAR(16, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'ENABLED'"), comment='状态')
    sort_code: Mapped[Optional[int]] = mapped_column(Integer, server_default=text("'0'"), comment='排序')
    extra: Mapped[Optional[str]] = mapped_column(Text(collation='utf8mb4_general_ci'), comment='扩展信息')
    is_deleted: Mapped[Optional[str]] = mapped_column(VARCHAR(8, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'NO'"), comment='逻辑删除')
    created_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment='创建时间')
    created_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='创建用户')
    updated_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment='更新时间')
    updated_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='更新用户')


class RalRolePermission(Base):
    __tablename__ = 'ral_role_permission'
    __table_args__ = (
        Index('uk_role_permission', 'role_id', 'permission_id', unique=True),
        Index('idx_permission_id', 'permission_id'),
        {'comment': '角色-权限关联'}
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), primary_key=True, comment='主键')
    role_id: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='角色ID')
    permission_id: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='权限ID')
    scope: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'ALL'"), comment='数据范围：ALL-全部，CUSTOM-自定义，ORG-本组织，ORG_AND_BELOW-本组织及以下，SELF-本人')
    custom_scope_group_ids: Mapped[Optional[str]] = mapped_column(Text(collation='utf8mb4_general_ci'), comment='自定义数据范围组ID列表(JSON数组)，scope=CUSTOM时生效')
    is_deleted: Mapped[Optional[str]] = mapped_column(VARCHAR(8, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'NO'"), comment='逻辑删除')
    created_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment='创建时间')
    created_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='创建用户')


class RalRoleResource(Base):
    __tablename__ = 'ral_role_resource'
    __table_args__ = (
        Index('uk_role_resource', 'role_id', 'resource_id', unique=True),
        Index('idx_resource_id', 'resource_id'),
        {'comment': '角色-资源关联'}
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), primary_key=True, comment='主键')
    role_id: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='角色ID')
    resource_id: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='资源ID')
    is_deleted: Mapped[Optional[str]] = mapped_column(VARCHAR(8, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'NO'"), comment='逻辑删除')
    created_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment='创建时间')
    created_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='创建用户')
