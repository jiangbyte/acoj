"""remove account is_superuser column"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op


revision: str = "7f4c2a1b9d8e"
down_revision: str | Sequence[str] | None = "1069e089cd54"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.drop_column("sys_account", "is_superuser")


def downgrade() -> None:
    op.add_column(
        "sys_account",
        sa.Column(
            "is_superuser",
            sa.Boolean(),
            nullable=False,
            server_default=sa.false(),
            comment="是否超级管理员",
        ),
    )
    op.alter_column("sys_account", "is_superuser", server_default=None)
