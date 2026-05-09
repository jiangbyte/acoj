from typing import Optional
import datetime

from sqlalchemy import DateTime, Integer, BigInteger, Text
from sqlalchemy.dialects.mysql import VARCHAR, LONGTEXT
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    pass


class SysFile(Base):
    __tablename__ = "sys_file"
    __table_args__ = {"comment": "文件"}

    id: Mapped[str] = mapped_column(VARCHAR(32), primary_key=True, comment="主键")
    engine: Mapped[Optional[str]] = mapped_column(VARCHAR(50), comment="存储引擎")
    bucket: Mapped[Optional[str]] = mapped_column(VARCHAR(255), comment="存储桶")
    file_key: Mapped[Optional[str]] = mapped_column(VARCHAR(500), comment="文件Key")
    name: Mapped[Optional[str]] = mapped_column(Text, comment="文件名称")
    suffix: Mapped[Optional[str]] = mapped_column(VARCHAR(50), comment="文件后缀")
    size_kb: Mapped[Optional[int]] = mapped_column(BigInteger, comment="文件大小kb")
    size_info: Mapped[Optional[str]] = mapped_column(VARCHAR(50), comment="文件大小（格式化后）")
    obj_name: Mapped[Optional[str]] = mapped_column(Text, comment="文件的对象名（唯一名称）")
    storage_path: Mapped[Optional[str]] = mapped_column(Text, comment="文件存储路径")
    download_path: Mapped[Optional[str]] = mapped_column(Text, comment="文件下载路径")
    is_download_auth: Mapped[Optional[int]] = mapped_column(Integer, comment="文件下载是否需要授权")
    thumbnail: Mapped[Optional[str]] = mapped_column(LONGTEXT, comment="图片缩略图")
    ext_json: Mapped[Optional[str]] = mapped_column(Text, comment="扩展信息")
    is_deleted: Mapped[Optional[str]] = mapped_column(VARCHAR(8), default="NO", comment="逻辑删除")
    created_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment="创建时间")
    created_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32), comment="创建用户")
    updated_at: Mapped[Optional[datetime.datetime]] = mapped_column(DateTime, comment="修改时间")
    updated_by: Mapped[Optional[str]] = mapped_column(VARCHAR(32), comment="修改用户")
