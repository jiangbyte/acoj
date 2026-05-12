from typing import Optional
import datetime

from sqlalchemy import DateTime, Integer, Text
from sqlalchemy.dialects.mysql import VARCHAR
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column
from core.enums import SoftDeleteEnum


class Base(DeclarativeBase):
    pass


class SysConfig(Base):
    __tablename__ = "sys_config"
    __table_args__ = {"comment": "系统配置"}

    id: Mapped[str] = mapped_column(VARCHAR(32), primary_key=True, comment="主键")
    config_key: Mapped[Optional[str]] = mapped_column(VARCHAR(255), comment="配置键")
    config_value: Mapped[Optional[str]] = mapped_column(Text, comment="配置值")
    category: Mapped[Optional[str]] = mapped_column(VARCHAR(255), comment="分类")
    remark: Mapped[Optional[str]] = mapped_column(VARCHAR(500), comment="备注")
    sort_code: Mapped[Optional[int]] = mapped_column(Integer, default=0, comment="排序码")
    ext_json: Mapped[Optional[str]] = mapped_column(Text, comment="扩展信息")
    is_deleted: Mapped[Optional[str]] = mapped_column(VARCHAR(8), default=SoftDeleteEnum.NO.value, comment="逻辑删除")
    created_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment="创建时间")
    created_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32), comment="创建用户")
    updated_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment="修改时间")
    updated_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32), comment="修改用户")
