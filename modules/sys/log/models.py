from typing import Optional
import datetime

from sqlalchemy import DateTime
from sqlalchemy.dialects.mysql import VARCHAR, LONGTEXT
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    pass


class SysLog(Base):
    __tablename__ = 'sys_log'
    __table_args__ = (
        {'comment': '操作日志'},
    )

    id: Mapped[str] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), primary_key=True, comment='主键')
    category: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='日志分类')
    name: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='日志名称')
    exe_status: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='执行状态')
    exe_message: Mapped[Optional[str]] = mapped_column(LONGTEXT(collation='utf8mb4_general_ci'), comment='具体消息')
    op_ip: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='操作ip')
    op_address: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='操作地址')
    op_browser: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='操作浏览器')
    op_os: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='操作系统')
    class_name: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='类名称')
    method_name: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='方法名称')
    req_method: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='请求方式')
    req_url: Mapped[Optional[str]] = mapped_column(LONGTEXT(collation='utf8mb4_general_ci'), comment='请求地址')
    param_json: Mapped[Optional[str]] = mapped_column(LONGTEXT(collation='utf8mb4_general_ci'), comment='请求参数')
    result_json: Mapped[Optional[str]] = mapped_column(LONGTEXT(collation='utf8mb4_general_ci'), comment='返回结果')
    op_time: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment='操作时间')
    trace_id: Mapped[Optional[str]] = mapped_column(VARCHAR(64, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='跟踪ID')
    op_user: Mapped[Optional[str]] = mapped_column(VARCHAR(255, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='操作人姓名')
    sign_data: Mapped[Optional[str]] = mapped_column(LONGTEXT(collation='utf8mb4_general_ci'), comment='签名数据')
    is_deleted: Mapped[Optional[str]] = mapped_column(VARCHAR(8, charset='utf8mb4', collation='utf8mb4_general_ci'), default="NO", comment='逻辑删除')
    created_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment='创建时间')
    created_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='创建用户')
    updated_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment='更新时间')
    updated_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32, charset='utf8mb4', collation='utf8mb4_general_ci'), comment='更新用户')
