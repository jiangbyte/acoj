"""rename oj_problem_language.enabled → status (ENABLED/DISABLED)"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "f7a809b1c2d3"
down_revision: str | Sequence[str] | None = "e6f7a809b1c2"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "oj_problem_language",
        sa.Column(
            "status",
            sa.String(length=32),
            nullable=False,
            server_default="ENABLED",
            comment="状态 ENABLED|DISABLED",
        ),
    )
    op.execute(
        sa.text(
            """
            UPDATE oj_problem_language
            SET status = CASE WHEN enabled IS TRUE THEN 'ENABLED' ELSE 'DISABLED' END
            """
        )
    )
    op.alter_column("oj_problem_language", "status", server_default=None)
    op.drop_column("oj_problem_language", "enabled")


def downgrade() -> None:
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
    op.execute(
        sa.text(
            """
            UPDATE oj_problem_language
            SET enabled = (status = 'ENABLED')
            """
        )
    )
    op.alter_column("oj_problem_language", "enabled", server_default=None)
    op.drop_column("oj_problem_language", "status")
