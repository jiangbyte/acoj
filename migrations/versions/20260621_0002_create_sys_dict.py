"""create sys dict"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "20260621_0002"
down_revision: str | Sequence[str] | None = "20260621_0001"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def _timestamp_columns() -> list[sa.Column]:
    return [
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.func.now(),
            nullable=False,
            comment="创建时间",
        ),
        sa.Column("created_by", sa.String(length=64), nullable=True, comment="创建人"),
        sa.Column(
            "updated_at",
            sa.DateTime(timezone=True),
            server_default=sa.func.now(),
            nullable=False,
            comment="更新时间",
        ),
        sa.Column("updated_by", sa.String(length=64), nullable=True, comment="更新人"),
    ]


def upgrade() -> None:
    op.create_table(
        "sys_dict",
        sa.Column("id", sa.String(length=32), nullable=False, comment="主键"),
        sa.Column("code", sa.String(length=50), nullable=False, comment="编码"),
        sa.Column("label", sa.String(length=255), nullable=True, comment="标签"),
        sa.Column("value", sa.String(length=255), nullable=True, comment="值"),
        sa.Column("color", sa.String(length=32), nullable=True, comment="颜色"),
        sa.Column("category", sa.String(length=64), nullable=True, comment="分类"),
        sa.Column("parent_id", sa.String(length=32), nullable=True, comment="父级ID"),
        sa.Column(
            "status",
            sa.String(length=16),
            nullable=False,
            server_default="ENABLED",
            comment="状态",
        ),
        sa.Column(
            "sort",
            sa.Integer(),
            nullable=False,
            server_default="0",
            comment="排序",
        ),
        *_timestamp_columns(),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_sys_dict")),
    )
    op.create_index("idx_sys_dict_code", "sys_dict", ["code"], unique=True)
    op.create_index("idx_sys_dict_category", "sys_dict", ["category"])
    op.create_index("idx_sys_dict_parent_id", "sys_dict", ["parent_id"])


def downgrade() -> None:
    op.drop_index("idx_sys_dict_parent_id", table_name="sys_dict")
    op.drop_index("idx_sys_dict_category", table_name="sys_dict")
    op.drop_index("idx_sys_dict_code", table_name="sys_dict")
    op.drop_table("sys_dict")
