"""add_created_by_updated_by_to_msg_terminal

Adds created_by and updated_by columns to msg_terminal table
to match the TimestampMixin inherited by the MsgTerminal ORM model.
"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op


revision: str = 'add_msg_terminal_timestamp'
down_revision: str | Sequence[str] | None = '954b50fe51de'
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column('msg_terminal', sa.Column('created_by', sa.String(length=64), nullable=True, comment='创建人'))
    op.add_column('msg_terminal', sa.Column('updated_by', sa.String(length=64), nullable=True, comment='更新人'))


def downgrade() -> None:
    op.drop_column('msg_terminal', 'updated_by')
    op.drop_column('msg_terminal', 'created_by')
