"""drop_portal_user_profile_level"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op


revision: str = '90d0bb170fa5'
down_revision: str | Sequence[str] | None = 'e9ef30a6a155'
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.drop_column('portal_user_profile', 'level')


def downgrade() -> None:
    op.add_column('portal_user_profile', sa.Column('level', sa.VARCHAR(length=32), autoincrement=False, nullable=True, comment='门户等级'))
