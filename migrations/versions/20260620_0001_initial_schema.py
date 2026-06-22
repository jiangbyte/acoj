"""initial schema"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "20260620_0001"
down_revision: str | Sequence[str] | None = None
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def _timestamp_columns() -> list[sa.Column]:
    return [
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.func.now(),
            nullable=False,
            comment="创建时间",
        ),
        sa.Column("created_by", sa.String(length=64), nullable=True, comment="创建人"),
        sa.Column(
            "updated_at",
            sa.DateTime(timezone=True),
            server_default=sa.func.now(),
            nullable=False,
            comment="更新时间",
        ),
        sa.Column("updated_by", sa.String(length=64), nullable=True, comment="更新人"),
    ]


def upgrade() -> None:
    op.create_table(
        "admin_user_profile",
        sa.Column("account_id", sa.String(length=64), nullable=False, comment="账户ID"),
        sa.Column("real_name", sa.String(length=64), nullable=True, comment="真实姓名"),
        sa.Column("avatar_url", sa.String(length=255), nullable=True, comment="头像地址"),
        sa.Column("title", sa.String(length=64), nullable=True, comment="岗位头衔"),
        sa.Column("employee_no", sa.String(length=64), nullable=True, comment="员工编号"),
        *_timestamp_columns(),
        sa.PrimaryKeyConstraint("account_id", name=op.f("pk_admin_user_profile")),
    )
    op.create_table(
        "portal_user_profile",
        sa.Column("account_id", sa.String(length=64), nullable=False, comment="账户ID"),
        sa.Column("nickname", sa.String(length=64), nullable=True, comment="门户昵称"),
        sa.Column("avatar_url", sa.String(length=255), nullable=True, comment="门户头像地址"),
        sa.Column("bio", sa.String(length=255), nullable=True, comment="个人简介"),
        sa.Column("level", sa.String(length=32), nullable=True, comment="门户等级"),
        *_timestamp_columns(),
        sa.PrimaryKeyConstraint("account_id", name=op.f("pk_portal_user_profile")),
    )
    op.create_table(
        "sys_account",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("account", sa.String(length=64), nullable=False, comment="账号"),
        sa.Column("password_hash", sa.String(length=255), nullable=False, comment="密码哈希"),
        sa.Column("account_type", sa.String(length=32), nullable=False, comment="账户类型"),
        sa.Column("account_status", sa.String(length=32), nullable=False, comment="账户状态"),
        sa.Column("name", sa.String(length=64), nullable=False, comment="姓名"),
        sa.Column("nickname", sa.String(length=64), nullable=True, comment="昵称"),
        sa.Column("avatar", sa.Text(), nullable=True, comment="头像"),
        sa.Column("signature", sa.Text(), nullable=True, comment="个性签名"),
        sa.Column("phone", sa.String(length=32), nullable=True, comment="手机号"),
        sa.Column("email", sa.String(length=128), nullable=True, comment="邮箱"),
        sa.Column("is_superuser", sa.Boolean(), nullable=False, comment="是否超级管理员"),
        sa.Column("cancelled_at", sa.DateTime(timezone=True), nullable=True, comment="注销时间"),
        sa.Column("cancelled_by", sa.String(length=64), nullable=True, comment="注销人"),
        sa.Column("cancel_reason", sa.Text(), nullable=True, comment="注销原因"),
        sa.Column("last_login_ip", sa.String(length=64), nullable=True, comment="上次登录IP"),
        sa.Column(
            "last_login_address",
            sa.String(length=255),
            nullable=True,
            comment="上次登录地点",
        ),
        sa.Column(
            "last_login_time",
            sa.DateTime(timezone=True),
            nullable=True,
            comment="上次登录时间",
        ),
        sa.Column("last_login_device", sa.Text(), nullable=True, comment="上次登录设备"),
        sa.Column("latest_login_ip", sa.String(length=64), nullable=True, comment="最新登录IP"),
        sa.Column(
            "latest_login_address",
            sa.String(length=255),
            nullable=True,
            comment="最新登录地点",
        ),
        sa.Column(
            "latest_login_time",
            sa.DateTime(timezone=True),
            nullable=True,
            comment="最新登录时间",
        ),
        sa.Column("latest_login_device", sa.Text(), nullable=True, comment="最新登录设备"),
        *_timestamp_columns(),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_sys_account")),
        sa.UniqueConstraint("account", name="uq_sys_account_account"),
    )
    op.create_table(
        "sys_account_dept_rel",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("account_id", sa.String(length=64), nullable=False, comment="账户ID"),
        sa.Column("dept_id", sa.String(length=64), nullable=False, comment="部门ID"),
        sa.Column("is_primary", sa.Boolean(), nullable=False, comment="是否主部门"),
        *_timestamp_columns(),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_sys_account_dept_rel")),
        sa.UniqueConstraint("account_id", "dept_id", name="uq_sys_account_dept_rel_account_dept"),
    )
    op.create_table(
        "sys_account_group_rel",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("account_id", sa.String(length=64), nullable=False, comment="账户ID"),
        sa.Column("group_id", sa.String(length=64), nullable=False, comment="账户组ID"),
        *_timestamp_columns(),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_sys_account_group_rel")),
        sa.UniqueConstraint(
            "account_id",
            "group_id",
            name="uq_sys_account_group_rel_account_group",
        ),
    )
    op.create_table(
        "sys_account_role_rel",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("account_id", sa.String(length=64), nullable=False, comment="账户ID"),
        sa.Column("role_id", sa.String(length=64), nullable=False, comment="角色ID"),
        *_timestamp_columns(),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_sys_account_role_rel")),
        sa.UniqueConstraint("account_id", "role_id", name="uq_sys_account_role_rel_account_role"),
    )
    op.create_table(
        "sys_dept",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("parent_id", sa.String(length=64), nullable=True, comment="父部门ID"),
        sa.Column("master_id", sa.String(length=64), nullable=True, comment="主管ID"),
        sa.Column("deputy_master_id", sa.String(length=64), nullable=True, comment="副主管ID"),
        sa.Column("name", sa.String(length=64), nullable=False, comment="部门名称"),
        sa.Column("code", sa.String(length=64), nullable=False, comment="部门编码"),
        sa.Column("category", sa.String(length=64), nullable=False, comment="部门类别"),
        sa.Column("sort", sa.Integer(), nullable=False, comment="排序"),
        sa.Column("is_virtual", sa.Boolean(), nullable=False, comment="是否虚拟部门"),
        sa.Column("status", sa.String(length=32), nullable=False, comment="状态"),
        sa.Column("extra", sa.JSON(), nullable=False, comment="扩展信息"),
        *_timestamp_columns(),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_sys_dept")),
        sa.UniqueConstraint("code", name="uq_sys_dept_code"),
    )
    op.create_table(
        "sys_file",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("object_name", sa.String(length=255), nullable=False, comment="对象存储路径"),
        sa.Column("original_name", sa.String(length=255), nullable=False, comment="原始文件名"),
        sa.Column("storage_provider", sa.String(length=32), nullable=False, comment="存储服务商"),
        sa.Column("bucket", sa.String(length=128), nullable=True, comment="存储桶"),
        sa.Column("content_type", sa.String(length=128), nullable=False, comment="文件类型"),
        sa.Column("size", sa.BigInteger(), nullable=False, comment="文件大小"),
        sa.Column("url", sa.String(length=1024), nullable=False, comment="访问地址"),
        *_timestamp_columns(),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_sys_file")),
        sa.UniqueConstraint("object_name", name=op.f("uq_sys_file_object_name")),
    )
    op.create_table(
        "sys_group",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("name", sa.String(length=64), nullable=False, comment="账户组名称"),
        sa.Column("description", sa.Text(), nullable=True, comment="描述"),
        sa.Column("status", sa.String(length=32), nullable=False, comment="状态"),
        sa.Column("extra", sa.JSON(), nullable=False, comment="扩展信息"),
        *_timestamp_columns(),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_sys_group")),
        sa.UniqueConstraint("name", name="uq_sys_group_name"),
    )
    op.create_table(
        "sys_group_role_rel",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("group_id", sa.String(length=64), nullable=False, comment="账户组ID"),
        sa.Column("role_id", sa.String(length=64), nullable=False, comment="角色ID"),
        *_timestamp_columns(),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_sys_group_role_rel")),
        sa.UniqueConstraint("group_id", "role_id", name="uq_sys_group_role_rel_group_role"),
    )
    op.create_table(
        "sys_position",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("name", sa.String(length=64), nullable=False, comment="职位名称"),
        sa.Column("code", sa.String(length=64), nullable=False, comment="职位编码"),
        sa.Column("category", sa.String(length=32), nullable=False, comment="职位类别"),
        sa.Column("sort", sa.Integer(), nullable=False, comment="排序"),
        sa.Column("is_virtual", sa.Boolean(), nullable=False, comment="是否虚拟职位"),
        sa.Column("status", sa.String(length=32), nullable=False, comment="状态"),
        sa.Column("description", sa.Text(), nullable=True, comment="职位描述"),
        sa.Column("extra", sa.JSON(), nullable=False, comment="扩展信息"),
        *_timestamp_columns(),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_sys_position")),
        sa.UniqueConstraint("code", name="uq_sys_position_code"),
    )
    op.create_table(
        "sys_resource",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("parent_id", sa.String(length=64), nullable=True, comment="父资源ID"),
        sa.Column("code", sa.String(length=64), nullable=False, comment="资源编码"),
        sa.Column("name", sa.String(length=64), nullable=False, comment="资源名称"),
        sa.Column("resource_type", sa.String(length=32), nullable=False, comment="资源类型"),
        sa.Column("module", sa.String(length=64), nullable=True, comment="所属模块"),
        sa.Column("path", sa.String(length=255), nullable=True, comment="路由路径"),
        sa.Column("component", sa.String(length=255), nullable=True, comment="前端组件"),
        sa.Column("redirect", sa.String(length=255), nullable=True, comment="重定向地址"),
        sa.Column("icon", sa.String(length=255), nullable=True, comment="图标"),
        sa.Column("href", sa.String(length=255), nullable=True, comment="外链地址"),
        sa.Column("sort", sa.Integer(), nullable=False, comment="排序"),
        sa.Column("is_visible", sa.Boolean(), nullable=False, comment="是否可见"),
        sa.Column("is_cache", sa.Boolean(), nullable=False, comment="是否缓存"),
        sa.Column("is_affix", sa.Boolean(), nullable=False, comment="是否固定标签"),
        sa.Column("status", sa.String(length=32), nullable=False, comment="状态"),
        sa.Column("description", sa.Text(), nullable=True, comment="描述"),
        sa.Column("extra", sa.JSON(), nullable=False, comment="扩展信息"),
        *_timestamp_columns(),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_sys_resource")),
        sa.UniqueConstraint("code", name="uq_sys_resource_code"),
    )
    op.create_table(
        "sys_resource_permission_rel",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("resource_id", sa.String(length=64), nullable=False, comment="资源ID"),
        sa.Column("permission_key", sa.String(length=128), nullable=False, comment="权限标识"),
        sa.Column("data_scope", sa.String(length=32), nullable=False, comment="数据范围"),
        sa.Column(
            "custom_scope_dept_ids",
            sa.JSON(),
            nullable=False,
            comment="自定义数据范围部门ID列表",
        ),
        sa.Column("sort", sa.Integer(), nullable=False, comment="排序"),
        sa.Column("status", sa.String(length=32), nullable=False, comment="状态"),
        sa.Column("description", sa.Text(), nullable=True, comment="描述"),
        *_timestamp_columns(),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_sys_resource_permission_rel")),
        sa.UniqueConstraint(
            "resource_id",
            "permission_key",
            name="uq_sys_resource_permission_rel_resource_permission",
        ),
    )
    op.create_table(
        "sys_role",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("code", sa.String(length=64), nullable=False, comment="角色编码"),
        sa.Column("name", sa.String(length=64), nullable=False, comment="角色名称"),
        sa.Column("category", sa.String(length=64), nullable=False, comment="角色分类"),
        sa.Column("scope_type", sa.String(length=32), nullable=False, comment="角色作用域类型"),
        sa.Column("owner_dept_id", sa.String(length=64), nullable=True, comment="所属部门ID"),
        sa.Column("sort", sa.Integer(), nullable=False, comment="排序"),
        sa.Column("status", sa.String(length=32), nullable=False, comment="状态"),
        sa.Column("is_builtin", sa.Boolean(), nullable=False, comment="是否内置角色"),
        sa.Column("description", sa.Text(), nullable=True, comment="描述"),
        sa.Column("extra", sa.JSON(), nullable=False, comment="扩展信息"),
        *_timestamp_columns(),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_sys_role")),
        sa.UniqueConstraint("code", name="uq_sys_role_code"),
    )
    op.create_table(
        "sys_subject_permission_grant_rel",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("subject_type", sa.String(length=32), nullable=False, comment="授权主体类型"),
        sa.Column("subject_id", sa.String(length=64), nullable=False, comment="授权主体ID"),
        sa.Column("permission_key", sa.String(length=128), nullable=False, comment="权限标识"),
        sa.Column("data_scope", sa.String(length=32), nullable=False, comment="数据范围"),
        sa.Column(
            "custom_scope_dept_ids",
            sa.JSON(),
            nullable=False,
            comment="自定义数据范围部门ID列表",
        ),
        sa.Column("effect", sa.String(length=32), nullable=False, comment="授权效果"),
        sa.Column("status", sa.String(length=32), nullable=False, comment="状态"),
        sa.Column("description", sa.Text(), nullable=True, comment="描述"),
        sa.Column("reason", sa.Text(), nullable=True, comment="授权原因"),
        sa.Column("expired_at", sa.DateTime(timezone=True), nullable=True, comment="失效时间"),
        *_timestamp_columns(),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_sys_subject_permission_grant_rel")),
        sa.UniqueConstraint(
            "subject_type",
            "subject_id",
            "permission_key",
            name="uq_sys_subject_permission_grant_rel_subject_permission",
        ),
    )
    op.create_table(
        "sys_subject_resource_grant_rel",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("subject_type", sa.String(length=32), nullable=False, comment="授权主体类型"),
        sa.Column("subject_id", sa.String(length=64), nullable=False, comment="授权主体ID"),
        sa.Column("resource_id", sa.String(length=64), nullable=False, comment="资源ID"),
        sa.Column("grant_mode", sa.String(length=32), nullable=False, comment="授权模式"),
        sa.Column("effect", sa.String(length=32), nullable=False, comment="授权效果"),
        sa.Column("status", sa.String(length=32), nullable=False, comment="状态"),
        sa.Column("description", sa.Text(), nullable=True, comment="描述"),
        sa.Column("expired_at", sa.DateTime(timezone=True), nullable=True, comment="失效时间"),
        *_timestamp_columns(),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_sys_subject_resource_grant_rel")),
        sa.UniqueConstraint(
            "subject_type",
            "subject_id",
            "resource_id",
            name="uq_sys_subject_resource_grant_rel_subject_resource",
        ),
    )


def downgrade() -> None:
    op.drop_table("sys_subject_resource_grant_rel")
    op.drop_table("sys_subject_permission_grant_rel")
    op.drop_table("sys_role")
    op.drop_table("sys_resource_permission_rel")
    op.drop_table("sys_resource")
    op.drop_table("sys_position")
    op.drop_table("sys_group_role_rel")
    op.drop_table("sys_group")
    op.drop_table("sys_file")
    op.drop_table("sys_dept")
    op.drop_table("sys_account_role_rel")
    op.drop_table("sys_account_group_rel")
    op.drop_table("sys_account_dept_rel")
    op.drop_table("sys_account")
    op.drop_table("portal_user_profile")
    op.drop_table("admin_user_profile")
