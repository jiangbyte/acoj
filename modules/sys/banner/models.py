from typing import Optional
import datetime

from sqlalchemy import DateTime, Integer, text
from sqlalchemy.dialects.mysql import TEXT, VARCHAR
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column

class Base(DeclarativeBase):
    pass


class SysBanner(Base):
    __tablename__ = 'sys_banner'
    __table_args__ = {'comment': '轮播图'}

    id: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), primary_key=True, comment='主键')
    title: Mapped[str] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='轮播标题')
    image: Mapped[str] = mapped_column(VARCHAR(500, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='轮播图片')
    category: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='轮播类别')
    type: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='轮播类型')
    position: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), nullable=False, comment='展示位置')
    url: Mapped[Optional[str]] = mapped_column(VARCHAR(500, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='跳转地址')
    link_type: Mapped[Optional[str]] = mapped_column(VARCHAR(16, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'URL'"), comment='链接类型')
    summary: Mapped[Optional[str]] = mapped_column(VARCHAR(500, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='轮播摘要')
    description: Mapped[Optional[str]] = mapped_column(TEXT(charset='utf8mb4', collation='utf8mb4_general_ci'), comment='轮播描述')
    sort_code: Mapped[Optional[int]] = mapped_column(Integer, server_default=text("'0'"), comment='排序')
    view_count: Mapped[Optional[int]] = mapped_column(Integer, server_default=text("'0'"), comment='浏览次数')
    click_count: Mapped[Optional[int]] = mapped_column(Integer, server_default=text("'0'"), comment='点击次数')
    is_deleted: Mapped[Optional[str]] = mapped_column(VARCHAR(8, charset='utf8mb4', collation='utf8mb4_general_ci'), server_default=text("'NO'"), comment='逻辑删除')
    created_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment='创建时间')
    created_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='创建用户')
    updated_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment='更新时间')
    updated_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='更新用户')
