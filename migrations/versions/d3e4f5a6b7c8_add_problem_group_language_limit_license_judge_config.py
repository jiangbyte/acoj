"""add problem_group, language_limit, license, judge_config tables; alter oj_problem"""

from collections.abc import Sequence

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

revision: str = "d3e4f5a6b7c8"
down_revision: str | None = "9b2f4c6d8e10"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    # ### 1. Create oj_problem_judge_config — split SPJ/interactor/remote from oj_problem
    op.create_table(
        "oj_problem_judge_config",
        sa.Column("id", sa.String(64), nullable=False, comment="主键"),
        sa.Column("problem_id", sa.String(64), nullable=False, comment="题目ID"),
        sa.Column("spj_language_id", sa.String(64), nullable=True, comment="Special Judge 语言ID"),
        sa.Column("spj_source", sa.Text(), nullable=True, comment="Special Judge 源码"),
        sa.Column("interactor_language_id", sa.String(64), nullable=True, comment="交互器语言ID"),
        sa.Column("interactor_source", sa.Text(), nullable=True, comment="交互器源码"),
        sa.Column("remote_provider", sa.String(64), nullable=True, comment="远程判题提供方"),
        sa.Column("remote_problem_id", sa.String(128), nullable=True, comment="远程题目ID"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False, comment="创建时间"),
        sa.Column("created_by", sa.String(64), nullable=True, comment="创建人"),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False, comment="更新时间"),
        sa.Column("updated_by", sa.String(64), nullable=True, comment="更新人"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("problem_id", name="uq_oj_problem_judge_config_problem"),
    )
    op.create_index("uq_oj_problem_judge_config_problem", "oj_problem_judge_config", ["problem_id"], unique=True)

    # ### 2. Create oj_problem_group — hierarchical problem grouping
    op.create_table(
        "oj_problem_group",
        sa.Column("id", sa.String(64), nullable=False, comment="主键"),
        sa.Column("parent_id", sa.String(64), nullable=True, comment="父分组ID"),
        sa.Column("code", sa.String(64), nullable=False, comment="分组编码"),
        sa.Column("name", sa.String(128), nullable=False, comment="分组名称"),
        sa.Column("full_name", sa.String(255), nullable=True, comment="完整名称"),
        sa.Column("description", sa.Text(), nullable=True, comment="描述"),
        sa.Column("icon", sa.String(256), nullable=True, comment="图标"),
        sa.Column("color", sa.String(32), nullable=True, comment="颜色"),
        sa.Column("sort", sa.Integer(), nullable=False, server_default=sa.text("0"), comment="排序"),
        sa.Column("status", sa.String(32), nullable=False, server_default=sa.text("'ENABLED'"), comment="状态"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False, comment="创建时间"),
        sa.Column("created_by", sa.String(64), nullable=True, comment="创建人"),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False, comment="更新时间"),
        sa.Column("updated_by", sa.String(64), nullable=True, comment="更新人"),
        sa.PrimaryKeyConstraint("id"),
    )
    op.create_index("ix_oj_problem_group_code", "oj_problem_group", ["code"], unique=True)
    op.create_index("ix_oj_problem_group_parent", "oj_problem_group", ["parent_id"])

    # ### 3. Create oj_language_limit — per-language time/memory/stack/output (all NOT NULL)
    op.create_table(
        "oj_language_limit",
        sa.Column("id", sa.String(64), nullable=False, comment="主键"),
        sa.Column("problem_id", sa.String(64), nullable=False, comment="题目ID"),
        sa.Column("language_id", sa.String(64), nullable=False, comment="语言ID"),
        sa.Column("time_limit_ms", sa.Integer(), nullable=False, comment="时间限制毫秒"),
        sa.Column("memory_limit_kb", sa.Integer(), nullable=False, comment="内存限制KB"),
        sa.Column("stack_limit_kb", sa.Integer(), nullable=False, comment="栈限制KB"),
        sa.Column("output_limit_kb", sa.Integer(), nullable=False, comment="输出限制KB"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False, comment="创建时间"),
        sa.Column("created_by", sa.String(64), nullable=True, comment="创建人"),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False, comment="更新时间"),
        sa.Column("updated_by", sa.String(64), nullable=True, comment="更新人"),
        sa.PrimaryKeyConstraint("id"),
    )
    op.create_index("uq_oj_language_limit", "oj_language_limit", ["problem_id", "language_id"], unique=True)
    op.create_index("ix_oj_language_limit_problem", "oj_language_limit", ["problem_id"])

    # ### 4. Create oj_license — problem content license
    op.create_table(
        "oj_license",
        sa.Column("id", sa.String(64), nullable=False, comment="主键"),
        sa.Column("key", sa.String(64), nullable=False, comment="协议标识"),
        sa.Column("name", sa.String(128), nullable=False, comment="协议名称"),
        sa.Column("display", sa.String(256), nullable=True, comment="显示名称"),
        sa.Column("url", sa.String(512), nullable=True, comment="协议链接"),
        sa.Column("icon", sa.String(512), nullable=True, comment="协议图标"),
        sa.Column("text", sa.Text(), nullable=True, comment="协议全文"),
        sa.Column("status", sa.String(32), nullable=False, server_default=sa.text("'ENABLED'"), comment="状态"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False, comment="创建时间"),
        sa.Column("created_by", sa.String(64), nullable=True, comment="创建人"),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False, comment="更新时间"),
        sa.Column("updated_by", sa.String(64), nullable=True, comment="更新人"),
        sa.PrimaryKeyConstraint("id"),
    )
    op.create_index("ix_oj_license_key", "oj_license", ["key"], unique=True)

    # ### 5. Alter oj_problem — add group_id, license_id; drop judge config columns
    op.add_column("oj_problem", sa.Column("group_id", sa.String(64), nullable=True, comment="分组ID"))
    op.add_column("oj_problem", sa.Column("license_id", sa.String(64), nullable=True, comment="授权协议ID"))

    op.drop_column("oj_problem", "spj_language_id")
    op.drop_column("oj_problem", "spj_source")
    op.drop_column("oj_problem", "interactor_language_id")
    op.drop_column("oj_problem", "interactor_source")
    op.drop_column("oj_problem", "remote_provider")
    op.drop_column("oj_problem", "remote_problem_id")

    # ### 6. Migrate existing SPI/interactor/remote data from oj_problem → oj_problem_judge_config
    op.execute("""
        INSERT INTO oj_problem_judge_config (id, problem_id, spj_language_id, spj_source,
            interactor_language_id, interactor_source, remote_provider, remote_problem_id)
        SELECT
            concat('cfg_', id),
            id,
            spj_language_id,
            spj_source,
            interactor_language_id,
            interactor_source,
            remote_provider,
            remote_problem_id
        FROM oj_problem
        WHERE spj_language_id IS NOT NULL
           OR spj_source IS NOT NULL
           OR interactor_language_id IS NOT NULL
           OR interactor_source IS NOT NULL
           OR remote_provider IS NOT NULL
           OR remote_problem_id IS NOT NULL
    """)


def downgrade() -> None:
    # Reverse order of upgrade
    op.drop_column("oj_problem", "license_id")
    op.drop_column("oj_problem", "group_id")
    op.add_column("oj_problem", sa.Column("remote_problem_id", sa.String(128), nullable=True, comment="远程题目ID"))
    op.add_column("oj_problem", sa.Column("remote_provider", sa.String(64), nullable=True, comment="远程判题提供方"))
    op.add_column("oj_problem", sa.Column("interactor_source", sa.Text(), nullable=True, comment="交互器源码"))
    op.add_column("oj_problem", sa.Column("interactor_language_id", sa.String(64), nullable=True, comment="交互器语言ID"))
    op.add_column("oj_problem", sa.Column("spj_source", sa.Text(), nullable=True, comment="Special Judge 源码"))
    op.add_column("oj_problem", sa.Column("spj_language_id", sa.String(64), nullable=True, comment="Special Judge 语言ID"))

    # Restore data from oj_problem_judge_config
    op.execute("""
        UPDATE oj_problem p
        SET
            spj_language_id = c.spj_language_id,
            spj_source = c.spj_source,
            interactor_language_id = c.interactor_language_id,
            interactor_source = c.interactor_source,
            remote_provider = c.remote_provider,
            remote_problem_id = c.remote_problem_id
        FROM oj_problem_judge_config c
        WHERE p.id = c.problem_id
    """)

    op.drop_table("oj_license")
    op.drop_table("oj_language_limit")
    op.drop_table("oj_problem_group")
    op.drop_table("oj_problem_judge_config")
