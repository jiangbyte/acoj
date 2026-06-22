"""create sys banner"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "20260621_0001"
down_revision: str | Sequence[str] | None = "20260620_0003"
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
        "sys_banner",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("title", sa.String(length=255), nullable=False, comment="标题"),
        sa.Column("image", sa.String(length=500), nullable=False, comment="图片地址"),
        sa.Column("url", sa.String(length=500), nullable=True, comment="跳转地址"),
        sa.Column("link_type", sa.String(length=16), nullable=False, comment="链接类型"),
        sa.Column("summary", sa.String(length=500), nullable=True, comment="摘要"),
        sa.Column("description", sa.Text(), nullable=True, comment="描述"),
        sa.Column("category", sa.String(length=32), nullable=False, comment="分类"),
        sa.Column("type", sa.String(length=32), nullable=False, comment="类型"),
        sa.Column("position", sa.String(length=32), nullable=False, comment="显示位置"),
        sa.Column("display_scope", sa.String(length=32), nullable=False, comment="显示端"),
        sa.Column(
            "sort",
            sa.Integer(),
            nullable=False,
            server_default="0",
            comment="排序",
        ),
        sa.Column(
            "interaction_count",
            sa.BigInteger(),
            nullable=False,
            server_default="0",
            comment="交互次数",
        ),
        sa.Column(
            "status",
            sa.String(length=32),
            nullable=False,
            server_default="ENABLED",
            comment="状态",
        ),
        sa.Column("start_at", sa.DateTime(timezone=True), nullable=True, comment="开始展示时间"),
        sa.Column("end_at", sa.DateTime(timezone=True), nullable=True, comment="结束展示时间"),
        *_timestamp_columns(),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_sys_banner")),
    )
    op.create_index(
        "ix_sys_banner_scope_position_status_sort",
        "sys_banner",
        ["display_scope", "position", "status", "sort"],
    )


def downgrade() -> None:
    op.drop_index("ix_sys_banner_scope_position_status_sort", table_name="sys_banner")
    op.drop_table("sys_banner")
