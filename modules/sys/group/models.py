from typing import Optional
import datetime

from sqlalchemy import DateTime, Index, Integer, Text, text
from sqlalchemy.dialects.mysql import VARCHAR
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column

class Base(DeclarativeBase):
    pass


class SysGroup(Base):
    __tablename__ = 'sys_group'
    __table_args__ = (
        Index('uk_code', 'code', unique=True),
        {'comment': '用户组'}
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), primary_key=True, comment='主键')
    code: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='用户组编码')
    name: Mapped[str] = mapped_column(VARCHAR(64, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='用户组名称')
    category: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='用户组类别')
    parent_id: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='父用户组ID')
    org_id: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='所属组织ID')
    description: Mapped[Optional[str]] = mapped_column(VARCHAR(500, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='用户组描述')
    status: Mapped[Optional[str]] = mapped_column(VARCHAR(16, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'ENABLED'"), comment='状态')
    sort_code: Mapped[Optional[int]] = mapped_column(Integer, server_default=text("'0'"), comment='排序')
    extra: Mapped[Optional[str]] = mapped_column(Text(collation='utf8mb4_general_ci'), comment='扩展信息')
    is_deleted: Mapped[Optional[str]] = mapped_column(VARCHAR(8, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'NO'"), comment='逻辑删除')
    created_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment='创建时间')
    created_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='创建用户')
    updated_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment='更新时间')
    updated_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='更新用户')


class RalGroupRole(Base):
    __tablename__ = 'ral_group_role'
    __table_args__ = (
        Index('uk_group_role', 'group_id', 'role_id', unique=True),
        Index('idx_role_id', 'role_id'),
        {'comment': '用户组-角色关联'}
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), primary_key=True, comment='主键')
    group_id: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='用户组ID')
    role_id: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='角色ID')
    scope: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=None, comment='数据范围覆盖：ALL-全部，CUSTOM-自定义，ORG-本组织，ORG_AND_BELOW-本组织及以下，SELF-本人。为空则继承 ral_role_permission 的配置')
    custom_scope_group_ids: Mapped[Optional[str]] = mapped_column(Text(collation='utf8mb4_general_ci'), comment='自定义数据范围组ID列表(JSON数组)，scope=CUSTOM时生效')
    is_deleted: Mapped[Optional[str]] = mapped_column(VARCHAR(8, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'NO'"), comment='逻辑删除')
    created_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment='创建时间')
    created_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='创建用户')
