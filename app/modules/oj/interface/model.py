from datetime import datetime

from sqlalchemy import Boolean, DateTime, Index, Integer, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjMiscConfig(Base, TimestampMixin):
    """在线评测杂项配置表。"""

    __tablename__ = "oj_misc_config"
    __table_args__ = (Index("ix_oj_misc_config_key", "key"),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    key: Mapped[str] = mapped_column(String(30), nullable=False, comment="键")
    value: Mapped[str] = mapped_column(Text, default="", nullable=False, comment="值")


class OjNavigationBar(Base, TimestampMixin):
    """导航栏表。"""

    __tablename__ = "oj_navigation_bar"
    __table_args__ = (
        UniqueConstraint("key", name="uq_oj_navigation_bar_key"),
        Index("ix_oj_navigation_bar_parent_order", "parent_id", "order"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    order: Mapped[int] = mapped_column(Integer, nullable=False, comment="排序")
    key: Mapped[str] = mapped_column(String(10), nullable=False, comment="标识")
    label: Mapped[str] = mapped_column(String(20), nullable=False, comment="标签")
    path: Mapped[str] = mapped_column(String(255), nullable=False, comment="链接路径")
    highlight_pattern: Mapped[str] = mapped_column(Text, nullable=False, comment="高亮匹配正则")
    parent_id: Mapped[str | None] = mapped_column(String(64), comment="父项")
    left_value: Mapped[int] = mapped_column(Integer, nullable=False, comment="嵌套集合左值")
    right_value: Mapped[int] = mapped_column(Integer, nullable=False, comment="嵌套集合右值")
    tree_id: Mapped[int] = mapped_column(Integer, nullable=False, comment="嵌套集合树ID")
    level: Mapped[int] = mapped_column(Integer, nullable=False, comment="嵌套集合层级")


class OjBlogPost(Base, TimestampMixin):
    """博客文章表。"""

    __tablename__ = "oj_blog_post"
    __table_args__ = (
        Index("ix_oj_blog_post_visible_publish_at", "visible", "publish_at"),
        Index("ix_oj_blog_post_sticky", "sticky"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    title: Mapped[str] = mapped_column(String(100), nullable=False, comment="文章标题")
    slug_name: Mapped[str] = mapped_column(String(128), nullable=False, comment="别名")
    visible: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否公开可见"
    )
    sticky: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False, comment="是否置顶")
    publish_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, comment="定时发布时间"
    )
    content: Mapped[str] = mapped_column(Text, nullable=False, comment="文章内容")
    summary: Mapped[str] = mapped_column(Text, default="", nullable=False, comment="文章摘要")
    open_graph_image: Mapped[str] = mapped_column(
        String(150), default="", nullable=False, comment="OpenGraph 图片"
    )


class OjBlogPostAuthorRel(Base):
    """博客文章作者关系表。"""

    __tablename__ = "oj_blog_post_author_rel"
    __table_args__ = (
        UniqueConstraint("post_id", "account_id", name="uq_oj_blog_post_author_rel_post_account"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    post_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="博客文章")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")
