"""drop_status_column_from_sys_codegen_plan

Removes the unused `status` column from the `sys_codegen_plan` table.
"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op


revision: str = 'drop_codegen_plan_status'
down_revision: str | Sequence[str] | None = 'add_msg_terminal_timestamp'
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.drop_column('sys_codegen_plan', 'status')


def downgrade() -> None:
    op.add_column(
        'sys_codegen_plan',
        sa.Column('status', sa.String(length=32), nullable=False, server_default='ENABLED', comment='状态'),
    )
