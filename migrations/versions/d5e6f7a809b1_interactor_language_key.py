"""add oj_problem_data.interactor_language_key (worker multi-lang interactor)"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "d5e6f7a809b1"
down_revision: str | Sequence[str] | None = "c4d5e6f7a809"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "oj_problem_data",
        sa.Column(
            "interactor_language_key",
            sa.String(length=32),
            nullable=True,
            comment="交互器语言 ID（worker LanguageConfig.key，如 cpp17）",
        ),
    )
    op.execute(
        sa.text(
            """
            UPDATE oj_problem_data
            SET interactor_language_key = 'cpp17'
            WHERE interactor_source IS NOT NULL
              AND TRIM(interactor_source) <> ''
              AND (interactor_language_key IS NULL OR interactor_language_key = '')
            """
        )
    )


def downgrade() -> None:
    op.drop_column("oj_problem_data", "interactor_language_key")
