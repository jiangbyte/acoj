"""add sys codegen"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op


revision: str = "8b1f4b2c9d10"
down_revision: str | Sequence[str] | None = "5de34ff9c199"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table(
        "sys_codegen_plan",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("name", sa.String(length=128), nullable=False, comment="方案名称"),
        sa.Column("gen_type", sa.String(length=32), nullable=False, comment="生成类型"),
        sa.Column("status", sa.String(length=32), nullable=False, comment="状态"),
        sa.Column("author", sa.String(length=64), nullable=False, comment="作者"),
        sa.Column("description", sa.Text(), nullable=True, comment="描述"),
        sa.Column("main_table", sa.String(length=128), nullable=False, comment="主表名"),
        sa.Column("main_pk", sa.String(length=128), nullable=False, comment="主表主键"),
        sa.Column("main_entity_name", sa.String(length=128), nullable=False, comment="主实体类名"),
        sa.Column("main_module_path", sa.String(length=255), nullable=False, comment="后端模块路径"),
        sa.Column("main_business_name", sa.String(length=128), nullable=False, comment="主业务名称"),
        sa.Column("api_prefix", sa.String(length=255), nullable=False, comment="接口前缀"),
        sa.Column("permission_prefix", sa.String(length=128), nullable=False, comment="权限前缀"),
        sa.Column("resource_module_id", sa.String(length=64), nullable=True, comment="资源模块ID"),
        sa.Column("parent_resource_id", sa.String(length=64), nullable=True, comment="父资源ID"),
        sa.Column("menu_name", sa.String(length=64), nullable=False, comment="菜单名称"),
        sa.Column("menu_path", sa.String(length=255), nullable=False, comment="菜单路径"),
        sa.Column("component_path", sa.String(length=255), nullable=False, comment="组件路径"),
        sa.Column("icon", sa.String(length=255), nullable=True, comment="菜单图标"),
        sa.Column("sort", sa.Integer(), nullable=False, comment="排序"),
        sa.Column("tree_parent_field", sa.String(length=128), nullable=True, comment="树父级字段"),
        sa.Column("tree_label_field", sa.String(length=128), nullable=True, comment="树展示字段"),
        sa.Column("sub_table", sa.String(length=128), nullable=True, comment="子表名"),
        sa.Column("sub_pk", sa.String(length=128), nullable=True, comment="子表主键"),
        sa.Column("sub_foreign_key", sa.String(length=128), nullable=True, comment="子表外键"),
        sa.Column("sub_entity_name", sa.String(length=128), nullable=True, comment="子实体类名"),
        sa.Column("sub_business_name", sa.String(length=128), nullable=True, comment="子业务名称"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False, comment="创建时间"),
        sa.Column("created_by", sa.String(length=64), nullable=True, comment="创建人"),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False, comment="更新时间"),
        sa.Column("updated_by", sa.String(length=64), nullable=True, comment="更新人"),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_sys_codegen_plan")),
        sa.UniqueConstraint("name", name="uq_sys_codegen_plan_name"),
    )
    op.create_index("ix_sys_codegen_plan_gen_type", "sys_codegen_plan", ["gen_type"], unique=False)
    op.create_index("ix_sys_codegen_plan_main_table", "sys_codegen_plan", ["main_table"], unique=False)

    op.create_table(
        "sys_codegen_field",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("plan_id", sa.String(length=64), nullable=False, comment="方案ID"),
        sa.Column("table_role", sa.String(length=16), nullable=False, comment="表角色"),
        sa.Column("column_name", sa.String(length=128), nullable=False, comment="字段名"),
        sa.Column("column_comment", sa.String(length=255), nullable=True, comment="字段注释"),
        sa.Column("db_type", sa.String(length=128), nullable=False, comment="数据库类型"),
        sa.Column("python_type", sa.String(length=64), nullable=False, comment="Python类型"),
        sa.Column("typescript_type", sa.String(length=64), nullable=False, comment="TypeScript类型"),
        sa.Column("form_widget", sa.String(length=32), nullable=False, comment="表单控件"),
        sa.Column("dict_code", sa.String(length=128), nullable=True, comment="字典编码"),
        sa.Column("query_operator", sa.String(length=32), nullable=True, comment="查询方式"),
        sa.Column("show_in_table", sa.Boolean(), nullable=False, comment="表格显示"),
        sa.Column("show_in_form", sa.Boolean(), nullable=False, comment="表单显示"),
        sa.Column("show_in_detail", sa.Boolean(), nullable=False, comment="详情显示"),
        sa.Column("show_in_query", sa.Boolean(), nullable=False, comment="查询显示"),
        sa.Column("is_primary_key", sa.Boolean(), nullable=False, comment="是否主键"),
        sa.Column("is_required", sa.Boolean(), nullable=False, comment="是否必填"),
        sa.Column("is_unique", sa.Boolean(), nullable=False, comment="是否唯一"),
        sa.Column("is_nullable", sa.Boolean(), nullable=False, comment="是否可空"),
        sa.Column("max_length", sa.Integer(), nullable=True, comment="最大长度"),
        sa.Column("sort", sa.Integer(), nullable=False, comment="排序"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False, comment="创建时间"),
        sa.Column("created_by", sa.String(length=64), nullable=True, comment="创建人"),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False, comment="更新时间"),
        sa.Column("updated_by", sa.String(length=64), nullable=True, comment="更新人"),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_sys_codegen_field")),
        sa.UniqueConstraint("plan_id", "table_role", "column_name", name="uq_sys_codegen_field_plan_role_column"),
    )
    op.create_index(
        "ix_sys_codegen_field_plan_role_sort",
        "sys_codegen_field",
        ["plan_id", "table_role", "sort"],
        unique=False,
    )


def downgrade() -> None:
    op.drop_index("ix_sys_codegen_field_plan_role_sort", table_name="sys_codegen_field")
    op.drop_table("sys_codegen_field")
    op.drop_index("ix_sys_codegen_plan_main_table", table_name="sys_codegen_plan")
    op.drop_index("ix_sys_codegen_plan_gen_type", table_name="sys_codegen_plan")
    op.drop_table("sys_codegen_plan")
