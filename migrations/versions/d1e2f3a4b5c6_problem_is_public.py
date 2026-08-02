"""oj_problem.is_public for public bank vs contest-only"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "d1e2f3a4b5c6"
down_revision: str | Sequence[str] | None = "c0d1e2f3a4b5"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "oj_problem",
        sa.Column(
            "is_public",
            sa.Boolean(),
            nullable=False,
            server_default=sa.text("false"),
            comment="是否公开题库可见",
        ),
    )
    op.create_index("ix_oj_problem_is_public", "oj_problem", ["is_public"])


def downgrade() -> None:
    op.drop_index("ix_oj_problem_is_public", table_name="oj_problem")
    op.drop_column("oj_problem", "is_public")
