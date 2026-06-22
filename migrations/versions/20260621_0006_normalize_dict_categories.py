"""normalize dict categories"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "20260621_0006"
down_revision: str | Sequence[str] | None = "20260621_0005"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.execute(
        sa.text(
            """
            update sys_dict
            set category = case
                when code like 'BANNER_%' then 'BIZ'
                else 'SYS'
            end
            """
        )
    )


def downgrade() -> None:
    pass
