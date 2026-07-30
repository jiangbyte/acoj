"""add oj_problem_data.judge_mode"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "b2c3d4e5f607"
down_revision: str | Sequence[str] | None = "a1b2c3d4e5f6"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "oj_problem_data",
        sa.Column(
            "judge_mode",
            sa.String(length=32),
            nullable=False,
            server_default="STANDARD",
            comment="判题模式 STANDARD|SPECIAL_JUDGE|INTERACTIVE",
        ),
    )
    op.execute(
        sa.text(
            """
            UPDATE oj_problem_data
            SET judge_mode = CASE
                WHEN interactor_file_id IS NOT NULL AND btrim(interactor_file_id) <> '' THEN 'INTERACTIVE'
                WHEN checker = 'custom' AND spj_file_id IS NOT NULL AND btrim(spj_file_id) <> '' THEN 'SPECIAL_JUDGE'
                ELSE 'STANDARD'
            END
            """
        )
    )
    op.alter_column("oj_problem_data", "judge_mode", server_default=None)


def downgrade() -> None:
    op.drop_column("oj_problem_data", "judge_mode")
