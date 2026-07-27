"""add storage config id to sys_file"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "a1b2c3d4e5f6"
down_revision: str | Sequence[str] | None = "90d0bb170fa5"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "sys_file",
        sa.Column("storage_config_id", sa.String(length=64), nullable=True, comment="存储配置 ID"),
    )
    conn = op.get_bind()
    default_id = conn.execute(
        sa.text("SELECT id FROM sys_storage_config WHERE is_default = true LIMIT 1")
    ).scalar()
    if default_id is None:
        default_id = "200101"
        conn.execute(
            sa.text(
                """
                INSERT INTO sys_storage_config (
                    id, name, provider, bucket, endpoint, access_key, secret_key,
                    region, use_ssl, base_url, public_path, local_root, is_default,
                    remark, sort_code
                ) VALUES (
                    :id, '本地存储', 'local', '', '', '', '',
                    '', false, '', '/api/v1/files', 'storage', true,
                    '本地文件系统存储', 0
                )
                """
            ),
            {"id": default_id},
        )
    conn.execute(
        sa.text("UPDATE sys_file SET storage_config_id = :id WHERE storage_config_id IS NULL"),
        {"id": default_id},
    )
    op.alter_column("sys_file", "storage_config_id", nullable=False)
    op.create_index(
        op.f("ix_sys_file_storage_config_id"),
        "sys_file",
        ["storage_config_id"],
        unique=False,
    )


def downgrade() -> None:
    op.drop_index(op.f("ix_sys_file_storage_config_id"), table_name="sys_file")
    op.drop_column("sys_file", "storage_config_id")
