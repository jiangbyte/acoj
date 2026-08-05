"""Add contest registration fields and table."""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "g4h5i6j7k8l9"
down_revision: str | Sequence[str] | None = "f3a4b5c6d7e8"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column("oj_contest", sa.Column("register_start", sa.DateTime(timezone=True), nullable=True, comment="报名开始时间"))
    op.add_column("oj_contest", sa.Column("register_end", sa.DateTime(timezone=True), nullable=True, comment="报名截止时间"))
    op.add_column(
        "oj_contest",
        sa.Column(
            "registration_mode",
            sa.String(length=16),
            nullable=False,
            server_default="AUTO",
            comment="报名模式 AUTO|REVIEW",
        ),
    )
    op.add_column(
        "oj_contest",
        sa.Column(
            "list_visibility",
            sa.String(length=16),
            nullable=False,
            server_default="PUBLIC",
            comment="列表可见性 PUBLIC|INVITE_ONLY",
        ),
    )

    op.create_table(
        "oj_contest_registration",
        sa.Column("id", sa.String(length=64), primary_key=True, comment="主键"),
        sa.Column("contest_id", sa.String(length=64), nullable=False, comment="竞赛ID"),
        sa.Column("account_id", sa.String(length=64), nullable=False, comment="账户ID"),
        sa.Column("status", sa.String(length=16), nullable=False, comment="PENDING|APPROVED|REJECTED|CANCELLED"),
        sa.Column("source", sa.String(length=16), nullable=False, comment="SELF|ADMIN"),
        sa.Column("applied_at", sa.DateTime(timezone=True), nullable=False, comment="申请时间"),
        sa.Column("reviewed_at", sa.DateTime(timezone=True), nullable=True, comment="审核时间"),
        sa.Column("reviewed_by", sa.String(length=64), nullable=True, comment="审核人"),
        sa.Column("remark", sa.Text(), nullable=True, comment="备注/拒绝原因"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.UniqueConstraint("contest_id", "account_id", name="uq_oj_contest_registration_contest_account"),
    )
    op.create_index(
        "ix_oj_contest_registration_contest_status",
        "oj_contest_registration",
        ["contest_id", "status"],
    )
    op.create_index("ix_oj_contest_registration_account", "oj_contest_registration", ["account_id"])

    op.execute(
        sa.text(
            """
            INSERT INTO oj_contest_registration (
                id, contest_id, account_id, status, source, applied_at, reviewed_at, reviewed_by, remark,
                created_at, updated_at
            )
            SELECT
                pc.id,
                pc.contest_id,
                pc.account_id,
                'APPROVED',
                'ADMIN',
                COALESCE(pc.created_at, now()),
                COALESCE(pc.created_at, now()),
                NULL,
                'migrated from private_contestant',
                COALESCE(pc.created_at, now()),
                COALESCE(pc.updated_at, now())
            FROM oj_contest_private_contestant pc
            ON CONFLICT (contest_id, account_id) DO NOTHING
            """
        )
    )


def downgrade() -> None:
    op.drop_index("ix_oj_contest_registration_account", table_name="oj_contest_registration")
    op.drop_index("ix_oj_contest_registration_contest_status", table_name="oj_contest_registration")
    op.drop_table("oj_contest_registration")
    op.drop_column("oj_contest", "list_visibility")
    op.drop_column("oj_contest", "registration_mode")
    op.drop_column("oj_contest", "register_end")
    op.drop_column("oj_contest", "register_start")
