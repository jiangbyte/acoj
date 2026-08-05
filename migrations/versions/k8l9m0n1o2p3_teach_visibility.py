"""Add visibility column to class / course / team."""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "k8l9m0n1o2p3"
down_revision: str | Sequence[str] | None = "j7k8l9m0n1o2"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "oj_class",
        sa.Column(
            "visibility",
            sa.String(length=16),
            nullable=False,
            server_default="PRIVATE",
            comment="PUBLIC|PRIVATE",
        ),
    )
    op.create_index("ix_oj_class_visibility", "oj_class", ["visibility"])

    op.add_column(
        "oj_course",
        sa.Column(
            "visibility",
            sa.String(length=16),
            nullable=False,
            server_default="PRIVATE",
            comment="PUBLIC|PRIVATE",
        ),
    )
    op.create_index("ix_oj_course_visibility", "oj_course", ["visibility"])

    op.add_column(
        "oj_team",
        sa.Column(
            "visibility",
            sa.String(length=16),
            nullable=False,
            server_default="PRIVATE",
            comment="PUBLIC|PRIVATE",
        ),
    )
    op.create_index("ix_oj_team_visibility", "oj_team", ["visibility"])


def downgrade() -> None:
    op.drop_index("ix_oj_team_visibility", table_name="oj_team")
    op.drop_column("oj_team", "visibility")
    op.drop_index("ix_oj_course_visibility", table_name="oj_course")
    op.drop_column("oj_course", "visibility")
    op.drop_index("ix_oj_class_visibility", table_name="oj_class")
    op.drop_column("oj_class", "visibility")
