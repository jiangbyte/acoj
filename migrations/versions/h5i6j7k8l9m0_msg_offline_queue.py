"""Add msg_offline_message_queue for WS offline delivery."""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "h5i6j7k8l9m0"
down_revision: str | Sequence[str] | None = "g4h5i6j7k8l9"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    conn = op.get_bind()
    exists = conn.execute(
        sa.text(
            "SELECT 1 FROM information_schema.tables "
            "WHERE table_schema = 'public' AND table_name = 'msg_offline_message_queue'"
        )
    ).scalar()
    if exists:
        return

    op.create_table(
        "msg_offline_message_queue",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("message_id", sa.String(length=64), nullable=False, comment="消息ID"),
        sa.Column("conversation_id", sa.String(length=64), nullable=False, comment="会话ID"),
        sa.Column("target_account_type", sa.String(length=32), nullable=False, comment="目标账户类型"),
        sa.Column("target_account_id", sa.String(length=64), nullable=False, comment="目标账户ID"),
        sa.Column("event_type", sa.String(length=32), nullable=False, comment="事件类型"),
        sa.Column("event_payload", sa.JSON(), nullable=False, comment="事件载荷"),
        sa.Column("status", sa.String(length=32), nullable=False, server_default="PENDING", comment="状态"),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, comment="创建时间"),
        sa.Column("delivered_at", sa.DateTime(timezone=True), nullable=True, comment="投递时间"),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_msg_offline_message_queue")),
    )
    op.create_index(
        "ix_msg_offline_target_status",
        "msg_offline_message_queue",
        ["target_account_type", "target_account_id", "status", "created_at"],
        unique=False,
    )


def downgrade() -> None:
    conn = op.get_bind()
    exists = conn.execute(
        sa.text(
            "SELECT 1 FROM information_schema.tables "
            "WHERE table_schema = 'public' AND table_name = 'msg_offline_message_queue'"
        )
    ).scalar()
    if not exists:
        return
    op.drop_index("ix_msg_offline_target_status", table_name="msg_offline_message_queue")
    op.drop_table("msg_offline_message_queue")
