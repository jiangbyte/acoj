"""submission performance query indexes"""

from collections.abc import Sequence

from alembic import op

revision: str = "i6j7k8l9m0n1"
down_revision: str | Sequence[str] | None = "d1e2f3a4b5c6"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_index(
        "ix_oj_submission_problem_lang_result_time",
        "oj_submission",
        ["problem_id", "language_key", "result", "time_ms"],
    )
    op.create_index(
        "ix_oj_submission_contest_problem_lang_result_time",
        "oj_submission",
        ["contest_id", "problem_id", "language_key", "result", "time_ms"],
    )


def downgrade() -> None:
    op.drop_index("ix_oj_submission_contest_problem_lang_result_time", table_name="oj_submission")
    op.drop_index("ix_oj_submission_problem_lang_result_time", table_name="oj_submission")
