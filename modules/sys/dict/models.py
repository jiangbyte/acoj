from typing import Optional
import datetime

from sqlalchemy import DateTime, Index, Integer, text
from sqlalchemy.dialects.mysql import VARCHAR
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column

class Base(DeclarativeBase):
    pass


class SysDict(Base):
    __tablename__ = 'sys_dict'
    __table_args__ = (
        Index('uk_code', 'code', unique=True),
        {'comment': '字典'}
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), primary_key=True, comment='主键')
    code: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='字典编码')
    label: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='字典标签')
    value: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='字典值')
    color: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='字典颜色')
    category: Mapped[Optional[str]] = mapped_column(VARCHAR(64, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='字典分类')
    parent_id: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='父字典ID')
    status: Mapped[Optional[str]] = mapped_column(VARCHAR(16, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'ENABLED'"), comment='状态')
    sort_code: Mapped[Optional[int]] = mapped_column(Integer, server_default=text("'0'"), comment='排序')
    is_deleted: Mapped[Optional[str]] = mapped_column(VARCHAR(8, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'NO'"), comment='逻辑删除')
    created_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment='创建时间')
    created_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='创建用户')
    updated_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment='更新时间')
    updated_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='更新用户')
