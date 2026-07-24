"""Modify notification and announcement target columns for multi-select support.

Changes:
- msg_notification: drop target_account_id, change target_account_type to JSON target_account_types
- msg_announcement: change target_account_type to JSON target_account_types
- Both: add target_dept_ids, target_role_ids JSON arrays
"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = 'msg_target_multi_select'
down_revision: str | Sequence[str] | None = 'drop_codegen_plan_status'
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    # === msg_notification ===
    op.drop_column('msg_notification', 'target_account_id')
    op.drop_column('msg_notification', 'target_account_type')
    op.add_column('msg_notification', sa.Column('target_account_types', sa.JSON, nullable=False, server_default=sa.text("'[]'::json"), comment='目标账户类型列表'))
    op.add_column('msg_notification', sa.Column('target_account_ids', sa.JSON, nullable=False, server_default=sa.text("'[]'::json"), comment='目标账户ID列表'))
    op.add_column('msg_notification', sa.Column('target_dept_ids', sa.JSON, nullable=False, server_default=sa.text("'[]'::json"), comment='目标部门ID列表'))
    op.add_column('msg_notification', sa.Column('target_role_ids', sa.JSON, nullable=False, server_default=sa.text("'[]'::json"), comment='目标角色ID列表'))

    # === msg_announcement ===
    op.drop_column('msg_announcement', 'target_account_type')
    op.add_column('msg_announcement', sa.Column('target_account_types', sa.JSON, nullable=False, server_default=sa.text("'[]'::json"), comment='目标账户类型列表'))
    op.add_column('msg_announcement', sa.Column('target_account_ids', sa.JSON, nullable=False, server_default=sa.text("'[]'::json"), comment='目标账户ID列表'))
    op.add_column('msg_announcement', sa.Column('target_dept_ids', sa.JSON, nullable=False, server_default=sa.text("'[]'::json"), comment='目标部门ID列表'))
    op.add_column('msg_announcement', sa.Column('target_role_ids', sa.JSON, nullable=False, server_default=sa.text("'[]'::json"), comment='目标角色ID列表'))


def downgrade() -> None:
    # === msg_announcement ===
    op.drop_column('msg_announcement', 'target_role_ids')
    op.drop_column('msg_announcement', 'target_dept_ids')
    op.drop_column('msg_announcement', 'target_account_ids')
    op.drop_column('msg_announcement', 'target_account_types')
    op.add_column('msg_announcement', sa.Column('target_account_type', sa.String(32), nullable=True, comment='目标账户类型'))

    # === msg_notification ===
    op.drop_column('msg_notification', 'target_role_ids')
    op.drop_column('msg_notification', 'target_dept_ids')
    op.drop_column('msg_notification', 'target_account_ids')
    op.drop_column('msg_notification', 'target_account_types')
    op.add_column('msg_notification', sa.Column('target_account_type', sa.String(32), nullable=True, comment='目标账户类型'))
    op.add_column('msg_notification', sa.Column('target_account_id', sa.String(64), nullable=True, comment='目标账户ID'))
