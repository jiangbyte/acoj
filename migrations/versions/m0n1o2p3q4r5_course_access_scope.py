"""Add course access_scope OPEN|CLASS."""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "m0n1o2p3q4r5"
down_revision: str | Sequence[str] | None = "l9m0n1o2p3q4"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "oj_course",
        sa.Column(
            "access_scope",
            sa.String(length=16),
            nullable=False,
            server_default="CLASS",
            comment="OPEN公开课|CLASS私有课",
        ),
    )
    op.create_index("ix_oj_course_access_scope", "oj_course", ["access_scope"])


def downgrade() -> None:
    op.drop_index("ix_oj_course_access_scope", table_name="oj_course")
    op.drop_column("oj_course", "access_scope")
