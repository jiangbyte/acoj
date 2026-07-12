"""add resource color"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op


revision: str = "8b3d0f6a2c91"
down_revision: str | Sequence[str] | None = "3f7a9d2c1b84"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "sys_resource",
        sa.Column("color", sa.String(length=32), nullable=True, comment="颜色"),
    )
    op.execute(
        """
        UPDATE sys_resource
        SET is_visible = TRUE
        WHERE resource_type IN ('CATALOG', 'MENU', 'PAGE')
        """
    )


def downgrade() -> None:
    op.drop_column("sys_resource", "color")
