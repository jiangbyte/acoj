"""fix resource route visibility"""

from collections.abc import Sequence

from alembic import op


revision: str = "1d4e8f2a9b70"
down_revision: str | Sequence[str] | None = "8b3d0f6a2c91"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.execute(
        """
        UPDATE sys_resource
        SET is_visible = TRUE
        WHERE resource_type IN ('CATALOG', 'MENU', 'PAGE')
        """
    )


def downgrade() -> None:
    pass
