"""rename banner sort field"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op
from sqlalchemy import inspect

revision: str = "20260621_0003"
down_revision: str | Sequence[str] | None = "20260621_0002"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    bind = op.get_bind()
    columns = {column["name"] for column in inspect(bind).get_columns("sys_banner")}
    if "sort_code" not in columns:
        return

    op.drop_index("ix_sys_banner_scope_position_status_sort", table_name="sys_banner")
    op.alter_column(
        "sys_banner",
        "sort_code",
        new_column_name="sort",
        existing_type=sa.BigInteger(),
        type_=sa.Integer(),
        existing_nullable=False,
        existing_server_default="0",
        existing_comment="排序码",
        comment="排序",
    )
    op.create_index(
        "ix_sys_banner_scope_position_status_sort",
        "sys_banner",
        ["display_scope", "position", "status", "sort"],
    )


def downgrade() -> None:
    bind = op.get_bind()
    columns = {column["name"] for column in inspect(bind).get_columns("sys_banner")}
    if "sort" not in columns:
        return

    op.drop_index("ix_sys_banner_scope_position_status_sort", table_name="sys_banner")
    op.alter_column(
        "sys_banner",
        "sort",
        new_column_name="sort_code",
        existing_type=sa.Integer(),
        type_=sa.BigInteger(),
        existing_nullable=False,
        existing_server_default="0",
        existing_comment="排序",
        comment="排序码",
    )
    op.create_index(
        "ix_sys_banner_scope_position_status_sort",
        "sys_banner",
        ["display_scope", "position", "status", "sort_code"],
    )
