"""drop unused OJ legacy columns unused by acoj-worker"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op
from sqlalchemy.dialects import postgresql

revision: str = "a8b9c0d1e2f3"
down_revision: str | Sequence[str] | None = "f7a809b1c2d3"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.drop_column("oj_problem", "short_circuit")

    op.drop_column("oj_problem_data", "generator_file_id")
    op.drop_column("oj_problem_data", "checker")
    op.drop_column("oj_problem_data", "checker_args")
    op.drop_column("oj_problem_data", "output_prefix")
    op.drop_column("oj_problem_data", "output_limit")
    op.drop_column("oj_problem_data", "enable_unicode")
    op.drop_column("oj_problem_data", "disable_big_math")
    op.drop_column("oj_problem_data", "feedback")

    op.drop_column("oj_problem_test_case", "checker")
    op.drop_column("oj_problem_test_case", "checker_args")
    op.drop_column("oj_problem_test_case", "generator_args")
    op.drop_column("oj_problem_test_case", "output_prefix")
    op.drop_column("oj_problem_test_case", "output_limit")


def downgrade() -> None:
    op.add_column(
        "oj_problem",
        sa.Column("short_circuit", sa.Boolean(), nullable=False, server_default=sa.text("false")),
    )
    op.alter_column("oj_problem", "short_circuit", server_default=None)

    op.add_column(
        "oj_problem_data",
        sa.Column("generator_file_id", sa.String(length=512), nullable=True),
    )
    op.add_column(
        "oj_problem_data",
        sa.Column("checker", sa.String(length=32), nullable=False, server_default="standard"),
    )
    op.add_column(
        "oj_problem_data",
        sa.Column(
            "checker_args",
            postgresql.JSON(astext_type=sa.Text()),
            nullable=False,
            server_default=sa.text("'{}'::json"),
        ),
    )
    op.add_column("oj_problem_data", sa.Column("output_prefix", sa.Integer(), nullable=True))
    op.add_column("oj_problem_data", sa.Column("output_limit", sa.Integer(), nullable=True))
    op.add_column("oj_problem_data", sa.Column("enable_unicode", sa.Boolean(), nullable=True))
    op.add_column("oj_problem_data", sa.Column("disable_big_math", sa.Boolean(), nullable=True))
    op.add_column("oj_problem_data", sa.Column("feedback", sa.Text(), nullable=True))
    op.alter_column("oj_problem_data", "checker", server_default=None)
    op.alter_column("oj_problem_data", "checker_args", server_default=None)

    op.add_column(
        "oj_problem_test_case",
        sa.Column("checker", sa.String(length=32), nullable=True),
    )
    op.add_column(
        "oj_problem_test_case",
        sa.Column("checker_args", postgresql.JSON(astext_type=sa.Text()), nullable=True),
    )
    op.add_column("oj_problem_test_case", sa.Column("generator_args", sa.Text(), nullable=True))
    op.add_column("oj_problem_test_case", sa.Column("output_prefix", sa.Integer(), nullable=True))
    op.add_column("oj_problem_test_case", sa.Column("output_limit", sa.Integer(), nullable=True))
