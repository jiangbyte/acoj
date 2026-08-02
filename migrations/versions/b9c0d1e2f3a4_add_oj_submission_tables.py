"""add oj_submission tables (shared practice/contest/trial)"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op
from sqlalchemy.dialects import postgresql

revision: str = "b9c0d1e2f3a4"
down_revision: str | Sequence[str] | None = "a8b9c0d1e2f3"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table(
        "oj_submission",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("user_id", sa.String(length=64), nullable=False, comment="提交账户ID"),
        sa.Column("problem_id", sa.String(length=64), nullable=False, comment="题目ID"),
        sa.Column("language_key", sa.String(length=32), nullable=False, comment="语言 key"),
        sa.Column("kind", sa.String(length=16), nullable=False, comment="OFFICIAL|TRIAL"),
        sa.Column("status", sa.String(length=16), nullable=False, comment="QUEUED|JUDGING|COMPLETED|FAILED"),
        sa.Column("result", sa.String(length=16), nullable=True, comment="AC|WA|TLE|…"),
        sa.Column("score", sa.Float(), nullable=False, server_default="0", comment="得分"),
        sa.Column("time_ms", sa.Integer(), nullable=False, server_default="0", comment="耗时 ms"),
        sa.Column("memory_kb", sa.Integer(), nullable=False, server_default="0", comment="内存 KB"),
        sa.Column("compile_output", sa.Text(), nullable=True, comment="编译输出"),
        sa.Column("error", sa.Text(), nullable=True, comment="错误信息"),
        sa.Column("contest_id", sa.String(length=64), nullable=True, comment="竞赛ID（denorm）"),
        sa.Column("case_points", sa.Float(), nullable=False, server_default="0", comment="测例得分合计"),
        sa.Column("case_total", sa.Float(), nullable=False, server_default="0", comment="测例满分合计"),
        sa.Column("locked_at", sa.DateTime(timezone=True), nullable=True, comment="锁定后禁止重判"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False, comment="创建时间"),
        sa.Column("created_by", sa.String(length=64), nullable=True, comment="创建人"),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False, comment="更新时间"),
        sa.Column("updated_by", sa.String(length=64), nullable=True, comment="更新人"),
        sa.PrimaryKeyConstraint("id"),
    )
    op.create_index(
        "ix_oj_submission_problem_user_created",
        "oj_submission",
        ["problem_id", "user_id", "created_at"],
    )
    op.create_index(
        "ix_oj_submission_contest_problem_user_score",
        "oj_submission",
        ["contest_id", "problem_id", "user_id", "score"],
    )
    op.create_index("ix_oj_submission_user_result", "oj_submission", ["user_id", "result"])
    op.create_index("ix_oj_submission_kind_created", "oj_submission", ["kind", "created_at"])

    op.create_table(
        "oj_submission_source",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("submission_id", sa.String(length=64), nullable=False, comment="提交ID"),
        sa.Column("source", sa.Text(), nullable=False, comment="源代码"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False, comment="创建时间"),
        sa.Column("created_by", sa.String(length=64), nullable=True, comment="创建人"),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False, comment="更新时间"),
        sa.Column("updated_by", sa.String(length=64), nullable=True, comment="更新人"),
        sa.ForeignKeyConstraint(["submission_id"], ["oj_submission.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("submission_id"),
    )

    op.create_table(
        "oj_submission_case",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("submission_id", sa.String(length=64), nullable=False, comment="提交ID"),
        sa.Column("case_no", sa.Integer(), nullable=False, comment="测例编号"),
        sa.Column("test_case_id", sa.String(length=64), nullable=True, comment="oj_problem_test_case.id"),
        sa.Column("result", sa.String(length=16), nullable=True, comment="测例 verdict"),
        sa.Column("score", sa.Float(), nullable=False, server_default="0", comment="测例得分"),
        sa.Column("time_ms", sa.Integer(), nullable=False, server_default="0", comment="耗时 ms"),
        sa.Column("memory_kb", sa.Integer(), nullable=False, server_default="0", comment="内存 KB"),
        sa.Column("stdout_preview", sa.Text(), nullable=True, comment="stdout 预览"),
        sa.Column("stderr_preview", sa.Text(), nullable=True, comment="stderr 预览"),
        sa.Column("feedback", sa.String(length=255), nullable=True, comment="短反馈"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False, comment="创建时间"),
        sa.Column("created_by", sa.String(length=64), nullable=True, comment="创建人"),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False, comment="更新时间"),
        sa.Column("updated_by", sa.String(length=64), nullable=True, comment="更新人"),
        sa.ForeignKeyConstraint(["submission_id"], ["oj_submission.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("submission_id", "case_no", name="uq_oj_submission_case_no"),
    )
    op.create_index("ix_oj_submission_case_submission_id", "oj_submission_case", ["submission_id"])

    op.create_table(
        "oj_contest_submission",
        sa.Column("id", sa.String(length=64), nullable=False, comment="主键"),
        sa.Column("submission_id", sa.String(length=64), nullable=False, comment="提交ID"),
        sa.Column("contest_problem_id", sa.String(length=64), nullable=False, comment="竞赛题目ID"),
        sa.Column("participation_id", sa.String(length=64), nullable=False, comment="参赛记录ID"),
        sa.Column("points", sa.Float(), nullable=False, server_default="0", comment="竞赛换算分"),
        sa.Column("extra", postgresql.JSONB(astext_type=sa.Text()), nullable=True, comment="扩展"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False, comment="创建时间"),
        sa.Column("created_by", sa.String(length=64), nullable=True, comment="创建人"),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False, comment="更新时间"),
        sa.Column("updated_by", sa.String(length=64), nullable=True, comment="更新人"),
        sa.ForeignKeyConstraint(["submission_id"], ["oj_submission.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("submission_id"),
    )


def downgrade() -> None:
    op.drop_table("oj_contest_submission")
    op.drop_index("ix_oj_submission_case_submission_id", table_name="oj_submission_case")
    op.drop_table("oj_submission_case")
    op.drop_table("oj_submission_source")
    op.drop_index("ix_oj_submission_kind_created", table_name="oj_submission")
    op.drop_index("ix_oj_submission_user_result", table_name="oj_submission")
    op.drop_index("ix_oj_submission_contest_problem_user_score", table_name="oj_submission")
    op.drop_index("ix_oj_submission_problem_user_created", table_name="oj_submission")
    op.drop_table("oj_submission")
