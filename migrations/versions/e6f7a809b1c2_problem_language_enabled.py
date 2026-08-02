"""add oj_problem_language.enabled"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "e6f7a809b1c2"
down_revision: str | Sequence[str] | None = "d5e6f7a809b1"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "oj_problem_language",
        sa.Column(
            "enabled",
            sa.Boolean(),
            nullable=False,
            server_default=sa.text("true"),
            comment="是否对提交/试测开放",
        ),
    )
    op.alter_column("oj_problem_language", "enabled", server_default=None)


def downgrade() -> None:
    op.drop_column("oj_problem_language", "enabled")
