"""add judge tables"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "0002"
down_revision: str | Sequence[str] | None = "0001"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table(
        "oj_judge_node",
        sa.Column("id", sa.String(length=64), nullable=False),
        sa.Column("node_id", sa.String(length=128), nullable=False),
        sa.Column("name", sa.String(length=128), nullable=False),
        sa.Column("base_url", sa.String(length=512), nullable=True),
        sa.Column("version", sa.String(length=64), nullable=False),
        sa.Column("status", sa.String(length=32), nullable=False),
        sa.Column("enabled", sa.Boolean(), nullable=False),
        sa.Column("cpu_core", sa.Integer(), nullable=False),
        sa.Column("load", sa.Float(), nullable=False),
        sa.Column("running_tasks", sa.Integer(), nullable=False),
        sa.Column("supported_languages", sa.JSON(), nullable=False),
        sa.Column("supported_features", sa.JSON(), nullable=False),
        sa.Column("last_heartbeat_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column(
            "updated_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_oj_judge_node")),
        sa.UniqueConstraint("node_id", name=op.f("uq_oj_judge_node_node_id")),
    )
    op.create_index("ix_oj_judge_node_last_heartbeat", "oj_judge_node", ["last_heartbeat_at"])
    op.create_index("ix_oj_judge_node_status_enabled", "oj_judge_node", ["status", "enabled"])

    op.create_table(
        "oj_judge_task",
        sa.Column("id", sa.String(length=64), nullable=False),
        sa.Column("submission_id", sa.String(length=64), nullable=False),
        sa.Column("problem_id", sa.String(length=64), nullable=False),
        sa.Column("problem_version", sa.String(length=64), nullable=False),
        sa.Column("node_id", sa.String(length=128), nullable=True),
        sa.Column("status", sa.String(length=32), nullable=False),
        sa.Column("priority", sa.Integer(), nullable=False),
        sa.Column("attempt", sa.Integer(), nullable=False),
        sa.Column("payload", sa.JSON(), nullable=False),
        sa.Column("locked_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("started_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("finished_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("error_message", sa.Text(), nullable=True),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column(
            "updated_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_oj_judge_task")),
    )
    op.create_index(
        "ix_oj_judge_task_status_priority_created",
        "oj_judge_task",
        ["status", "priority", "created_at"],
    )
    op.create_index("ix_oj_judge_task_submission", "oj_judge_task", ["submission_id"])

    op.create_table(
        "oj_submission",
        sa.Column("id", sa.String(length=64), nullable=False),
        sa.Column("problem_id", sa.String(length=64), nullable=False),
        sa.Column("user_id", sa.String(length=64), nullable=False),
        sa.Column("language", sa.String(length=64), nullable=False),
        sa.Column("source", sa.Text(), nullable=False),
        sa.Column("status", sa.String(length=32), nullable=False),
        sa.Column("score", sa.Float(), nullable=False),
        sa.Column("time_ms", sa.Integer(), nullable=False),
        sa.Column("memory_kb", sa.Integer(), nullable=False),
        sa.Column("compile_message", sa.Text(), nullable=True),
        sa.Column("judger_id", sa.String(length=128), nullable=True),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column(
            "updated_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_oj_submission")),
    )
    op.create_index("ix_oj_submission_problem_user", "oj_submission", ["problem_id", "user_id"])
    op.create_index("ix_oj_submission_status_created", "oj_submission", ["status", "created_at"])

    op.create_table(
        "oj_submission_case",
        sa.Column("id", sa.String(length=64), nullable=False),
        sa.Column("submission_id", sa.String(length=64), nullable=False),
        sa.Column("case_no", sa.Integer(), nullable=False),
        sa.Column("batch_no", sa.Integer(), nullable=True),
        sa.Column("status", sa.String(length=32), nullable=False),
        sa.Column("score", sa.Float(), nullable=False),
        sa.Column("total_score", sa.Float(), nullable=False),
        sa.Column("time_ms", sa.Integer(), nullable=False),
        sa.Column("memory_kb", sa.Integer(), nullable=False),
        sa.Column("feedback", sa.Text(), nullable=True),
        sa.Column("output_preview", sa.Text(), nullable=True),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column(
            "updated_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_oj_submission_case")),
    )
    op.create_index(
        "ix_oj_submission_case_submission",
        "oj_submission_case",
        ["submission_id", "case_no"],
    )


def downgrade() -> None:
    op.drop_index("ix_oj_submission_case_submission", table_name="oj_submission_case")
    op.drop_table("oj_submission_case")
    op.drop_index("ix_oj_submission_status_created", table_name="oj_submission")
    op.drop_index("ix_oj_submission_problem_user", table_name="oj_submission")
    op.drop_table("oj_submission")
    op.drop_index("ix_oj_judge_task_submission", table_name="oj_judge_task")
    op.drop_index("ix_oj_judge_task_status_priority_created", table_name="oj_judge_task")
    op.drop_table("oj_judge_task")
    op.drop_index("ix_oj_judge_node_status_enabled", table_name="oj_judge_node")
    op.drop_index("ix_oj_judge_node_last_heartbeat", table_name="oj_judge_node")
    op.drop_table("oj_judge_node")
