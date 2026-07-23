"""add_timestamp_columns_to_msg_conversation_member

Adds created_at, created_by, updated_at, updated_by columns to
msg_conversation_member table to match the TimestampMixin inherited
by the MsgConversationMember ORM model.
"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op


revision: str = '954b50fe51de'
down_revision: str | Sequence[str] | None = '0001_message_refactor'
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column('msg_conversation_member', sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=False, comment='创建时间'))
    op.add_column('msg_conversation_member', sa.Column('created_by', sa.String(length=64), nullable=True, comment='创建人'))
    op.add_column('msg_conversation_member', sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=False, comment='更新时间'))
    op.add_column('msg_conversation_member', sa.Column('updated_by', sa.String(length=64), nullable=True, comment='更新人'))


def downgrade() -> None:
    op.drop_column('msg_conversation_member', 'updated_by')
    op.drop_column('msg_conversation_member', 'updated_at')
    op.drop_column('msg_conversation_member', 'created_by')
    op.drop_column('msg_conversation_member', 'created_at')
