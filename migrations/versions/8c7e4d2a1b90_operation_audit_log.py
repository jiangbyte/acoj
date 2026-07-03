"""operation audit log"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "8c7e4d2a1b90"
down_revision: str | Sequence[str] | None = "d16fb6ef0f85"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table(
        "sys_operation_audit_log",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("module", sa.String(length=64), nullable=False, comment="模块"),
        sa.Column("resource_type", sa.String(length=128), nullable=True, comment="资源类型"),
        sa.Column("resource_id", sa.String(length=128), nullable=True, comment="资源ID"),
        sa.Column("action", sa.String(length=64), nullable=False, comment="操作"),
        sa.Column("summary", sa.String(length=255), nullable=True, comment="摘要"),
        sa.Column("before_data", sa.JSON(), nullable=True, comment="变更前数据"),
        sa.Column("after_data", sa.JSON(), nullable=True, comment="变更后数据"),
        sa.Column("account_id", sa.String(length=64), nullable=True, comment="操作账号ID"),
        sa.Column("account_type", sa.String(length=32), nullable=True, comment="操作账号类型"),
        sa.Column("request_id", sa.String(length=64), nullable=True, comment="请求ID"),
        sa.Column("ip", sa.String(length=64), nullable=True, comment="客户端IP"),
        sa.Column("user_agent", sa.String(length=512), nullable=True, comment="User-Agent"),
        sa.Column("success", sa.Boolean(), nullable=False, comment="是否成功"),
        sa.Column("error_message", sa.Text(), nullable=True, comment="错误信息"),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
            comment="创建时间",
        ),
        sa.PrimaryKeyConstraint("id"),
    )
    op.create_index(
        "idx_sys_operation_audit_account_id",
        "sys_operation_audit_log",
        ["account_id"],
        unique=False,
    )
    op.create_index(
        "idx_sys_operation_audit_created_at",
        "sys_operation_audit_log",
        ["created_at"],
        unique=False,
    )
    op.create_index(
        "idx_sys_operation_audit_module_action",
        "sys_operation_audit_log",
        ["module", "action"],
        unique=False,
    )
    op.create_index(
        "idx_sys_operation_audit_resource",
        "sys_operation_audit_log",
        ["resource_type", "resource_id"],
        unique=False,
    )


def downgrade() -> None:
    op.drop_index("idx_sys_operation_audit_resource", table_name="sys_operation_audit_log")
    op.drop_index("idx_sys_operation_audit_module_action", table_name="sys_operation_audit_log")
    op.drop_index("idx_sys_operation_audit_created_at", table_name="sys_operation_audit_log")
    op.drop_index("idx_sys_operation_audit_account_id", table_name="sys_operation_audit_log")
    op.drop_table("sys_operation_audit_log")
