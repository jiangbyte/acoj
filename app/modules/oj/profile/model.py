from datetime import datetime

from sqlalchemy import Boolean, DateTime, Index, Integer, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjOrganization(Base, TimestampMixin):
    """组织表。"""

    __tablename__ = "oj_organization"
    __table_args__ = (
        Index("ix_oj_organization_name", "name"),
        Index("ix_oj_organization_slug_name", "slug_name"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    name: Mapped[str] = mapped_column(String(128), nullable=False, comment="组织名称")
    slug_name: Mapped[str] = mapped_column(String(128), nullable=False, comment="组织别名")
    short_name: Mapped[str] = mapped_column(String(20), nullable=False, comment="简称")
    about: Mapped[str] = mapped_column(Text, nullable=False, comment="组织描述")
    founded_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, comment="创建日期"
    )
    is_open: Mapped[bool] = mapped_column(
        Boolean, default=True, nullable=False, comment="组织是否开放"
    )
    slots: Mapped[int | None] = mapped_column(Integer, comment="最大容量")
    access_code: Mapped[str | None] = mapped_column(String(7), comment="访问码")
    logo_override_image: Mapped[str] = mapped_column(
        String(150), default="", nullable=False, comment="Logo 覆盖图"
    )
    class_required: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否要求班级"
    )


class OjClass(Base, TimestampMixin):
    """班级表。"""

    __tablename__ = "oj_class"
    __table_args__ = (
        UniqueConstraint("name", name="uq_oj_class_name"),
        Index("ix_oj_class_organization_name", "organization_id", "name"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    organization_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="组织")
    name: Mapped[str] = mapped_column(String(128), nullable=False, comment="班级名称")
    slug_name: Mapped[str] = mapped_column(String(128), nullable=False, comment="班级别名")
    description: Mapped[str] = mapped_column(
        Text, default="", nullable=False, comment="班级描述"
    )
    is_active: Mapped[bool] = mapped_column(
        Boolean, default=True, nullable=False, comment="班级是否启用"
    )
    access_code: Mapped[str | None] = mapped_column(String(7), comment="访问码")


# class OjAccount(Base, TimestampMixin):
#     """账户表。"""

#     __tablename__ = "oj_account"
#     __table_args__ = (
#         UniqueConstraint("account_id", name="uq_oj_account_account_id"),
#         Index("ix_oj_account_unlisted_performance", "is_unlisted", "performance_points"),
#         Index("ix_oj_account_unlisted_rating", "is_unlisted", "rating"),
#         Index("ix_oj_account_unlisted_problem_count", "is_unlisted", "problem_count"),
#     )

#     id: Mapped[str] = mapped_column(
#         String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
#     )
#     account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="关联账户")
#     about: Mapped[str | None] = mapped_column(Text, comment="个人简介")
#     timezone: Mapped[str] = mapped_column(String(50), nullable=False, comment="时区")
#     language_id: Mapped[str] = mapped_column(
#         String(64), nullable=False, comment="首选语言"
#     )
#     points: Mapped[float] = mapped_column(Float, default=0, nullable=False, comment="分数")
#     performance_points: Mapped[float] = mapped_column(
#         Float, default=0, nullable=False, comment="表现分"
#     )
#     problem_count: Mapped[int] = mapped_column(
#         Integer, default=0, nullable=False, comment="题目数量"
#     )
#     ace_theme: Mapped[str] = mapped_column(
#         String(30), default="auto", nullable=False, comment="Ace 编辑器主题"
#     )
#     site_theme: Mapped[str] = mapped_column(
#         String(10), default="auto", nullable=False, comment="站点主题"
#     )
#     last_access_at: Mapped[datetime] = mapped_column(
#         DateTime(timezone=True), nullable=False, comment="最近访问时间"
#     )
#     ip: Mapped[str | None] = mapped_column(String(64), comment="最近 IP")
#     display_rank: Mapped[str] = mapped_column(
#         String(10), default="user", nullable=False, comment="显示等级"
#     )
#     mute: Mapped[bool] = mapped_column(
#         Boolean, default=False, nullable=False, comment="是否禁言评论"
#     )
#     is_unlisted: Mapped[bool] = mapped_column(
#         Boolean, default=False, nullable=False, comment="是否隐藏排名"
#     )
#     is_banned_from_problem_voting: Mapped[bool] = mapped_column(
#         Boolean, default=False, nullable=False, comment="是否禁止题目分值投票"
#     )
#     rating: Mapped[int | None] = mapped_column(Integer, comment="评级")
#     account_script: Mapped[str] = mapped_column(
#         Text, default="", nullable=False, comment="账户脚本"
#     )
#     current_contest_id: Mapped[str | None] = mapped_column(String(64), comment="当前比赛")
#     math_engine: Mapped[str] = mapped_column(String(4), nullable=False, comment="数学渲染引擎")
#     is_totp_enabled: Mapped[bool] = mapped_column(
#         Boolean, default=False, nullable=False, comment="是否启用 TOTP 双因素认证"
#     )
#     is_webauthn_enabled: Mapped[bool] = mapped_column(
#         Boolean, default=False, nullable=False, comment="是否启用 WebAuthn 双因素认证"
#     )
#     totp_key: Mapped[str | None] = mapped_column(String(32), comment="TOTP 密钥")
#     scratch_codes: Mapped[str | None] = mapped_column(String(255), comment="备用验证码")
#     last_totp_timecode: Mapped[int] = mapped_column(
#         Integer, default=0, nullable=False, comment="最近 TOTP 时间码"
#     )
#     api_token: Mapped[str | None] = mapped_column(String(64), comment="API 令牌")
#     notes: Mapped[str | None] = mapped_column(Text, comment="内部备注")
#     data_downloaded_at: Mapped[datetime | None] = mapped_column(
#         DateTime(timezone=True), comment="最近数据下载时间"
#     )
#     username_display_override: Mapped[str] = mapped_column(
#         String(100), default="", nullable=False, comment="显示名称覆盖值"
#     )


# class OjWebAuthnCredential(Base, TimestampMixin):
#     """WebAuthn 凭证表。"""

#     __tablename__ = "oj_webauthn_credential"
#     __table_args__ = (UniqueConstraint("cred_id", name="uq_oj_webauthn_credential_cred_id"),)

#     id: Mapped[str] = mapped_column(
#         String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
#     )
#     account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")
#     name: Mapped[str] = mapped_column(String(100), nullable=False, comment="设备名称")
#     cred_id: Mapped[str] = mapped_column(String(255), nullable=False, comment="凭证ID")
#     public_key: Mapped[str] = mapped_column(Text, nullable=False, comment="公钥")
#     counter: Mapped[int] = mapped_column(BigInteger, nullable=False, comment="签名计数器")


class OjOrganizationRequest(Base, TimestampMixin):
    """组织加入申请表。"""

    __tablename__ = "oj_organization_request"
    __table_args__ = (
        Index("ix_oj_organization_request_org_state", "organization_id", "state"),
        Index("ix_oj_organization_request_account", "account_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")
    organization_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="组织")
    requested_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, comment="申请时间"
    )
    state: Mapped[str] = mapped_column(String(1), nullable=False, comment="状态")
    request_class_id: Mapped[str | None] = mapped_column(String(64), comment="班级")
    reason: Mapped[str] = mapped_column(Text, nullable=False, comment="原因")


class OjOrganizationAdminRel(Base):
    """组织管理员关系表。"""

    __tablename__ = "oj_organization_admin_rel"
    __table_args__ = (
        UniqueConstraint(
            "organization_id", "account_id", name="uq_oj_organization_admin_rel_org_account"
        ),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    organization_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="组织")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")


class OjOrganizationMemberRel(Base):
    """组织成员关系表。"""

    __tablename__ = "oj_organization_member_rel"
    __table_args__ = (
        UniqueConstraint(
            "organization_id", "account_id", name="uq_oj_organization_member_rel_org_account"
        ),
        Index("ix_oj_organization_member_rel_account", "account_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    organization_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="组织")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")
    sort: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="排序")


class OjClassAdminRel(Base):
    """班级管理员关系表。"""

    __tablename__ = "oj_class_admin_rel"
    __table_args__ = (
        UniqueConstraint("class_id", "account_id", name="uq_oj_class_admin_rel_class_account"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    class_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="班级")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")


class OjClassMemberRel(Base):
    """班级成员关系表。"""

    __tablename__ = "oj_class_member_rel"
    __table_args__ = (
        UniqueConstraint("class_id", "account_id", name="uq_oj_class_member_rel_class_account"),
        Index("ix_oj_class_member_rel_account", "account_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    class_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="班级")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")
