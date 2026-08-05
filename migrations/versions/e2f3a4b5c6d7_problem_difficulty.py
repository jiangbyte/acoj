"""oj_problem.difficulty + backfill user_count/ac_rate"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "e2f3a4b5c6d7"
down_revision: str | Sequence[str] | None = "d1e2f3a4b5c6"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "oj_problem",
        sa.Column(
            "difficulty",
            sa.String(length=16),
            nullable=False,
            server_default="Medium",
            comment="难度 Easy|Medium|Hard",
        ),
    )
    op.create_index("ix_oj_problem_difficulty", "oj_problem", ["difficulty"])

    # Backfill pass stats from non-trial submissions with a verdict.
    op.execute(
        sa.text(
            """
            WITH stats AS (
              SELECT
                problem_id,
                COUNT(*) AS total,
                COUNT(*) FILTER (WHERE result = 'AC') AS ac_cnt,
                COUNT(DISTINCT user_id) FILTER (WHERE result = 'AC') AS user_cnt
              FROM oj_submission
              WHERE kind <> 'TRIAL'
                AND result IS NOT NULL
              GROUP BY problem_id
            )
            UPDATE oj_problem AS p
            SET
              user_count = COALESCE(s.user_cnt, 0),
              ac_rate = CASE
                WHEN COALESCE(s.total, 0) = 0 THEN 0
                ELSE ROUND((s.ac_cnt::numeric / s.total::numeric) * 100, 2)
              END
            FROM stats AS s
            WHERE p.id = s.problem_id
            """
        )
    )


def downgrade() -> None:
    op.drop_index("ix_oj_problem_difficulty", table_name="oj_problem")
    op.drop_column("oj_problem", "difficulty")
