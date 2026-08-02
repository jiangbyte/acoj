"""contest module: formats, freeze, rating, clarifications, submit fields"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op
from sqlalchemy.dialects import postgresql

revision: str = "c0d1e2f3a4b5"
down_revision: str | Sequence[str] | None = "b9c0d1e2f3a4"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "oj_contest",
        sa.Column("freeze_seconds", sa.Integer(), nullable=True, comment="结束前封榜秒数"),
    )
    op.drop_column("oj_contest", "og_image")
    op.drop_column("oj_contest", "logo_override_image")
    op.drop_column("oj_contest", "problem_label_script")
    op.drop_column("oj_contest", "show_short_display")

    op.execute("UPDATE oj_contest SET format_name = 'atcoder' WHERE format_name IN ('ATOCCODER', 'atcoder')")
    op.execute("UPDATE oj_contest SET format_name = 'default' WHERE format_name IN ('ecole', 'ECOLE', 'ECOO', 'ecoo')")

    op.add_column(
        "oj_contest_participation",
        sa.Column(
            "rate_exclude",
            sa.Boolean(),
            nullable=False,
            server_default=sa.text("false"),
            comment="排除 Rating",
        ),
    )

    op.add_column(
        "oj_contest_submission",
        sa.Column(
            "is_pretest",
            sa.Boolean(),
            nullable=False,
            server_default=sa.text("false"),
            comment="是否仅 pretest",
        ),
    )

    op.add_column(
        "portal_user_profile",
        sa.Column("rating", sa.Integer(), nullable=True, comment="当前 Rating"),
    )

    op.create_table(
        "oj_contest_rating",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("contest_id", sa.String(length=64), nullable=False, comment="竞赛ID"),
        sa.Column("account_id", sa.String(length=64), nullable=False, comment="账户ID"),
        sa.Column("participation_id", sa.String(length=64), nullable=False, comment="参赛记录ID"),
        sa.Column("rank", sa.Integer(), nullable=False, comment="名次"),
        sa.Column("rating", sa.Integer(), nullable=False, comment="结算后 Rating"),
        sa.Column("delta", sa.Integer(), nullable=False, comment="Rating 变化"),
        sa.Column("performance", sa.Integer(), nullable=False, comment="本场表现分"),
        sa.Column("rated_at", sa.DateTime(timezone=True), nullable=False, comment="结算时间"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.PrimaryKeyConstraint("id", name="pk_oj_contest_rating"),
        sa.UniqueConstraint("participation_id", name="uq_oj_contest_rating_participation"),
        sa.UniqueConstraint("contest_id", "account_id", name="uq_oj_contest_rating_contest_account"),
    )
    op.create_index("ix_oj_contest_rating_contest", "oj_contest_rating", ["contest_id"])
    op.create_index("ix_oj_contest_rating_account", "oj_contest_rating", ["account_id"])

    op.create_table(
        "oj_contest_clarification",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("contest_id", sa.String(length=64), nullable=False, comment="竞赛ID"),
        sa.Column("problem_id", sa.String(length=64), nullable=True, comment="关联题目"),
        sa.Column("title", sa.String(length=200), nullable=False, comment="标题"),
        sa.Column("body", sa.Text(), nullable=False, comment="正文"),
        sa.Column("published_at", sa.DateTime(timezone=True), nullable=False, comment="发布时间"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.PrimaryKeyConstraint("id", name="pk_oj_contest_clarification"),
    )
    op.create_index("ix_oj_contest_clarification_contest", "oj_contest_clarification", ["contest_id"])

    op.create_table(
        "oj_contest_clarification_thread",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("contest_id", sa.String(length=64), nullable=False, comment="竞赛ID"),
        sa.Column("problem_id", sa.String(length=64), nullable=True, comment="关联题目"),
        sa.Column("account_id", sa.String(length=64), nullable=False, comment="提问账户"),
        sa.Column("title", sa.String(length=200), nullable=False, comment="标题"),
        sa.Column("status", sa.String(length=16), nullable=False, comment="OPEN|ANSWERED|CLOSED"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.PrimaryKeyConstraint("id", name="pk_oj_contest_clarification_thread"),
    )
    op.create_index("ix_oj_contest_clar_thread_contest", "oj_contest_clarification_thread", ["contest_id"])
    op.create_index("ix_oj_contest_clar_thread_status", "oj_contest_clarification_thread", ["contest_id", "status"])

    op.create_table(
        "oj_contest_clarification_message",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("thread_id", sa.String(length=64), nullable=False, comment="线程ID"),
        sa.Column("account_id", sa.String(length=64), nullable=False, comment="发送账户"),
        sa.Column("body", sa.Text(), nullable=False, comment="正文"),
        sa.Column(
            "is_staff",
            sa.Boolean(),
            nullable=False,
            server_default=sa.text("false"),
            comment="是否工作人员回复",
        ),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.PrimaryKeyConstraint("id", name="pk_oj_contest_clarification_message"),
    )
    op.create_index("ix_oj_contest_clar_msg_thread", "oj_contest_clarification_message", ["thread_id"])


def downgrade() -> None:
    op.drop_table("oj_contest_clarification_message")
    op.drop_table("oj_contest_clarification_thread")
    op.drop_table("oj_contest_clarification")
    op.drop_table("oj_contest_rating")
    op.drop_column("portal_user_profile", "rating")
    op.drop_column("oj_contest_submission", "is_pretest")
    op.drop_column("oj_contest_participation", "rate_exclude")
    op.add_column("oj_contest", sa.Column("show_short_display", sa.Boolean(), nullable=False, server_default=sa.text("false")))
    op.add_column("oj_contest", sa.Column("problem_label_script", sa.Text(), nullable=True))
    op.add_column("oj_contest", sa.Column("og_image", sa.String(length=255), nullable=True))
    op.add_column("oj_contest", sa.Column("logo_override_image", sa.String(length=255), nullable=True))
    op.drop_column("oj_contest", "freeze_seconds")
