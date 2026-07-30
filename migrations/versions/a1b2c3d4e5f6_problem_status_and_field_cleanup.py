"""problem status and field cleanup"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "a1b2c3d4e5f6"
down_revision: str | Sequence[str] | None = "571ebd1d248f"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "oj_problem",
        sa.Column(
            "status",
            sa.String(length=16),
            nullable=False,
            server_default="draft",
            comment="发布状态 draft|ready|published",
        ),
    )
    op.execute(
        sa.text(
            "UPDATE oj_problem SET status = CASE WHEN is_public IS TRUE THEN 'published' ELSE 'draft' END"
        )
    )
    op.create_index("ix_oj_problem_status", "oj_problem", ["status"], unique=False)
    op.drop_index("ix_oj_problem_is_public", table_name="oj_problem")
    op.drop_column("oj_problem", "is_public")
    op.drop_column("oj_problem", "is_manually_managed")
    op.drop_column("oj_problem", "is_full_markup")
    op.drop_column("oj_problem", "og_image")
    op.alter_column("oj_problem", "status", server_default=None)

    op.add_column(
        "oj_problem_language",
        sa.Column("time_limit_ms", sa.Integer(), nullable=True, comment="覆盖时间限制（毫秒），空则用题目默认"),
    )
    op.add_column(
        "oj_problem_language",
        sa.Column("memory_limit_kb", sa.Integer(), nullable=True, comment="覆盖内存限制（KB），空则用题目默认"),
    )
    op.execute(
        sa.text(
            """
            UPDATE oj_problem_language AS lang
            SET
                time_limit_ms = lim.time_limit_ms,
                memory_limit_kb = lim.memory_limit_kb
            FROM oj_problem_language_limit AS lim
            WHERE lang.problem_id = lim.problem_id
              AND lang.language_key = lim.language_key
            """
        )
    )
    op.execute(
        sa.text(
            """
            INSERT INTO oj_problem_language (
                id, problem_id, language_key, time_limit_ms, memory_limit_kb,
                created_at, created_by, updated_at, updated_by
            )
            SELECT
                lim.id, lim.problem_id, lim.language_key, lim.time_limit_ms, lim.memory_limit_kb,
                lim.created_at, lim.created_by, lim.updated_at, lim.updated_by
            FROM oj_problem_language_limit AS lim
            WHERE NOT EXISTS (
                SELECT 1 FROM oj_problem_language AS lang
                WHERE lang.problem_id = lim.problem_id
                  AND lang.language_key = lim.language_key
            )
            """
        )
    )
    op.drop_table("oj_problem_language_limit")

    op.drop_column("oj_problem_test_case", "input_file_id")
    op.drop_column("oj_problem_test_case", "output_file_id")

    op.alter_column(
        "oj_problem_data",
        "zip_file_id",
        new_column_name="zip_object_name",
        existing_type=sa.String(length=512),
        comment="导入用 zip 归档 storage key（仅 admin 重导入；不参与 MQ 判题）",
        existing_nullable=True,
    )


def downgrade() -> None:
    op.alter_column(
        "oj_problem_data",
        "zip_object_name",
        new_column_name="zip_file_id",
        existing_type=sa.String(length=512),
        comment="导入用 zip 归档 storage key（仅 admin 重导入；不参与 MQ 判题）",
        existing_nullable=True,
    )

    op.add_column(
        "oj_problem_test_case",
        sa.Column("output_file_id", sa.String(length=64), nullable=True, comment="输出文件元数据ID"),
    )
    op.add_column(
        "oj_problem_test_case",
        sa.Column("input_file_id", sa.String(length=64), nullable=True, comment="输入文件元数据ID"),
    )

    op.create_table(
        "oj_problem_language_limit",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("problem_id", sa.String(length=64), nullable=False, comment="题目ID"),
        sa.Column("language_key", sa.String(length=32), nullable=False, comment="语言标识"),
        sa.Column("time_limit_ms", sa.Integer(), nullable=False, comment="时间限制（毫秒）"),
        sa.Column("memory_limit_kb", sa.Integer(), nullable=False, comment="内存限制（KB）"),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, comment="创建时间"),
        sa.Column("created_by", sa.String(length=64), nullable=True, comment="创建人"),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False, comment="更新时间"),
        sa.Column("updated_by", sa.String(length=64), nullable=True, comment="更新人"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("problem_id", "language_key", name="uq_oj_problem_language_limit"),
    )
    op.execute(
        sa.text(
            """
            INSERT INTO oj_problem_language_limit (
                id, problem_id, language_key, time_limit_ms, memory_limit_kb,
                created_at, created_by, updated_at, updated_by
            )
            SELECT
                id, problem_id, language_key, time_limit_ms, memory_limit_kb,
                created_at, created_by, updated_at, updated_by
            FROM oj_problem_language
            WHERE time_limit_ms IS NOT NULL OR memory_limit_kb IS NOT NULL
            """
        )
    )
    op.drop_column("oj_problem_language", "memory_limit_kb")
    op.drop_column("oj_problem_language", "time_limit_ms")

    op.add_column(
        "oj_problem",
        sa.Column("og_image", sa.String(length=255), nullable=True, comment="OpenGraph 图片"),
    )
    op.add_column(
        "oj_problem",
        sa.Column(
            "is_full_markup",
            sa.Boolean(),
            nullable=False,
            server_default=sa.text("false"),
            comment="是否允许完整 Markup",
        ),
    )
    op.add_column(
        "oj_problem",
        sa.Column(
            "is_manually_managed",
            sa.Boolean(),
            nullable=False,
            server_default=sa.text("false"),
            comment="测试数据是否人工托管",
        ),
    )
    op.add_column(
        "oj_problem",
        sa.Column(
            "is_public",
            sa.Boolean(),
            nullable=False,
            server_default=sa.text("false"),
            comment="是否公开可见",
        ),
    )
    op.execute(sa.text("UPDATE oj_problem SET is_public = (status = 'published')"))
    op.create_index("ix_oj_problem_is_public", "oj_problem", ["is_public"], unique=False)
    op.drop_index("ix_oj_problem_status", table_name="oj_problem")
    op.drop_column("oj_problem", "status")
    op.alter_column("oj_problem", "is_public", server_default=None)
    op.alter_column("oj_problem", "is_manually_managed", server_default=None)
    op.alter_column("oj_problem", "is_full_markup", server_default=None)
