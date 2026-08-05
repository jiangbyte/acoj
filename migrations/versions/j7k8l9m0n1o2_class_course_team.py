"""Class, course, team teaching domain tables."""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "j7k8l9m0n1o2"
down_revision: str | Sequence[str] | None = ("h5i6j7k8l9m0", "i6j7k8l9m0n1")
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table(
        "oj_class",
        sa.Column("id", sa.String(length=64), primary_key=True, comment="主键"),
        sa.Column("code", sa.String(length=64), nullable=False, comment="班级编码"),
        sa.Column("name", sa.String(length=200), nullable=False, comment="班级名称"),
        sa.Column("summary", sa.Text(), nullable=True, comment="简介"),
        sa.Column("invite_code", sa.String(length=8), nullable=False, comment="邀请码"),
        sa.Column("status", sa.String(length=16), nullable=False, server_default="ENABLED", comment="ENABLED|DISABLED"),
        sa.Column("im_group_id", sa.String(length=64), nullable=True, comment="绑定 IM 群"),
        sa.Column("member_count", sa.Integer(), nullable=False, server_default="0", comment="成员数"),
        sa.Column("extra", sa.JSON(), nullable=False, server_default=sa.text("'{}'::json"), comment="扩展"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.UniqueConstraint("code", name="uq_oj_class_code"),
        sa.UniqueConstraint("invite_code", name="uq_oj_class_invite_code"),
    )
    op.create_index("ix_oj_class_status", "oj_class", ["status"])

    op.create_table(
        "oj_class_member",
        sa.Column("id", sa.String(length=64), primary_key=True, comment="主键"),
        sa.Column("class_id", sa.String(length=64), nullable=False, comment="班级ID"),
        sa.Column("account_id", sa.String(length=64), nullable=False, comment="Portal 账户ID"),
        sa.Column("role", sa.String(length=16), nullable=False, server_default="STUDENT", comment="STUDENT|ASSISTANT"),
        sa.Column("joined_at", sa.DateTime(timezone=True), nullable=False, comment="加入时间"),
        sa.Column("left_at", sa.DateTime(timezone=True), nullable=True, comment="离开时间"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.UniqueConstraint("class_id", "account_id", name="uq_oj_class_member_class_account"),
    )
    op.create_index("ix_oj_class_member_class_id", "oj_class_member", ["class_id"])
    op.create_index("ix_oj_class_member_account_id", "oj_class_member", ["account_id"])

    op.create_table(
        "oj_course",
        sa.Column("id", sa.String(length=64), primary_key=True, comment="主键"),
        sa.Column("class_id", sa.String(length=64), nullable=False, comment="班级ID"),
        sa.Column("name", sa.String(length=200), nullable=False, comment="课程名称"),
        sa.Column("summary", sa.Text(), nullable=True, comment="简介"),
        sa.Column("cover_url", sa.String(length=512), nullable=True, comment="封面"),
        sa.Column("status", sa.String(length=16), nullable=False, server_default="DRAFT", comment="DRAFT|PUBLISHED|ARCHIVED"),
        sa.Column("sort", sa.Integer(), nullable=False, server_default="0", comment="排序"),
        sa.Column("extra", sa.JSON(), nullable=False, server_default=sa.text("'{}'::json"), comment="扩展"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
    )
    op.create_index("ix_oj_course_class_id", "oj_course", ["class_id"])
    op.create_index("ix_oj_course_class_status", "oj_course", ["class_id", "status"])

    op.create_table(
        "oj_course_announcement",
        sa.Column("id", sa.String(length=64), primary_key=True, comment="主键"),
        sa.Column("course_id", sa.String(length=64), nullable=False, comment="课程ID"),
        sa.Column("title", sa.String(length=200), nullable=False, comment="标题"),
        sa.Column("content", sa.Text(), nullable=True, comment="内容"),
        sa.Column("status", sa.String(length=16), nullable=False, server_default="DRAFT", comment="DRAFT|PUBLISHED|REVOKED"),
        sa.Column("published_at", sa.DateTime(timezone=True), nullable=True, comment="发布时间"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
    )
    op.create_index("ix_oj_course_announcement_course_id", "oj_course_announcement", ["course_id"])

    op.create_table(
        "oj_course_task",
        sa.Column("id", sa.String(length=64), primary_key=True, comment="主键"),
        sa.Column("course_id", sa.String(length=64), nullable=False, comment="课程ID"),
        sa.Column("title", sa.String(length=200), nullable=False, comment="任务标题"),
        sa.Column("description", sa.Text(), nullable=True, comment="描述"),
        sa.Column("mode", sa.String(length=16), nullable=False, comment="REALTIME|ASYNC"),
        sa.Column("status", sa.String(length=16), nullable=False, server_default="DRAFT", comment="DRAFT|PUBLISHED|CLOSED"),
        sa.Column("open_at", sa.DateTime(timezone=True), nullable=True, comment="实时任务开放"),
        sa.Column("close_at", sa.DateTime(timezone=True), nullable=True, comment="实时任务关闭"),
        sa.Column("due_at", sa.DateTime(timezone=True), nullable=True, comment="异步任务截止"),
        sa.Column("sort", sa.Integer(), nullable=False, server_default="0", comment="排序"),
        sa.Column("extra", sa.JSON(), nullable=False, server_default=sa.text("'{}'::json"), comment="扩展"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
    )
    op.create_index("ix_oj_course_task_course_id", "oj_course_task", ["course_id"])
    op.create_index("ix_oj_course_task_course_status", "oj_course_task", ["course_id", "status"])

    op.create_table(
        "oj_course_task_problem",
        sa.Column("id", sa.String(length=64), primary_key=True, comment="主键"),
        sa.Column("task_id", sa.String(length=64), nullable=False, comment="任务ID"),
        sa.Column("problem_id", sa.String(length=64), nullable=False, comment="题目ID"),
        sa.Column("sort", sa.Integer(), nullable=False, server_default="0", comment="排序"),
        sa.Column("score", sa.Float(), nullable=True, comment="分值"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.UniqueConstraint("task_id", "problem_id", name="uq_oj_course_task_problem_task_problem"),
    )
    op.create_index("ix_oj_course_task_problem_task_id", "oj_course_task_problem", ["task_id"])

    op.create_table(
        "oj_course_task_progress",
        sa.Column("id", sa.String(length=64), primary_key=True, comment="主键"),
        sa.Column("task_id", sa.String(length=64), nullable=False, comment="任务ID"),
        sa.Column("account_id", sa.String(length=64), nullable=False, comment="账户ID"),
        sa.Column("solved_count", sa.Integer(), nullable=False, server_default="0", comment="已 AC 题数"),
        sa.Column("total_count", sa.Integer(), nullable=False, server_default="0", comment="总题数"),
        sa.Column("status", sa.String(length=16), nullable=False, server_default="NOT_STARTED", comment="进度状态"),
        sa.Column("finished_at", sa.DateTime(timezone=True), nullable=True, comment="完成时间"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.UniqueConstraint("task_id", "account_id", name="uq_oj_course_task_progress_task_account"),
    )
    op.create_index("ix_oj_course_task_progress_task_id", "oj_course_task_progress", ["task_id"])

    op.create_table(
        "oj_course_task_submission",
        sa.Column("id", sa.String(length=64), primary_key=True, comment="主键"),
        sa.Column("task_id", sa.String(length=64), nullable=False, comment="任务ID"),
        sa.Column("account_id", sa.String(length=64), nullable=False, comment="账户ID"),
        sa.Column("problem_id", sa.String(length=64), nullable=False, comment="题目ID"),
        sa.Column("submission_id", sa.String(length=64), nullable=False, comment="提交ID"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.UniqueConstraint("task_id", "account_id", "problem_id", name="uq_oj_course_task_submission_task_account_problem"),
    )
    op.create_index("ix_oj_course_task_submission_task_id", "oj_course_task_submission", ["task_id"])
    op.create_index("ix_oj_course_task_submission_submission_id", "oj_course_task_submission", ["submission_id"])

    op.create_table(
        "oj_team",
        sa.Column("id", sa.String(length=64), primary_key=True, comment="主键"),
        sa.Column("scope", sa.String(length=16), nullable=False, comment="INDEPENDENT|COURSE"),
        sa.Column("course_id", sa.String(length=64), nullable=True, comment="课内小组所属课程"),
        sa.Column("class_id", sa.String(length=64), nullable=True, comment="课内小组所属班级"),
        sa.Column("name", sa.String(length=200), nullable=False, comment="小组名称"),
        sa.Column("description", sa.Text(), nullable=True, comment="简介"),
        sa.Column("owner_id", sa.String(length=64), nullable=False, comment="所有者"),
        sa.Column("invite_code", sa.String(length=8), nullable=False, comment="邀请码"),
        sa.Column("im_group_id", sa.String(length=64), nullable=True, comment="绑定 IM 群"),
        sa.Column("status", sa.String(length=16), nullable=False, server_default="ENABLED", comment="ENABLED|DISABLED|DISSOLVED"),
        sa.Column("max_members", sa.Integer(), nullable=False, server_default="50", comment="最大成员数"),
        sa.Column("member_count", sa.Integer(), nullable=False, server_default="1", comment="成员数"),
        sa.Column("extra", sa.JSON(), nullable=False, server_default=sa.text("'{}'::json"), comment="扩展"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.UniqueConstraint("invite_code", name="uq_oj_team_invite_code"),
    )
    op.create_index("ix_oj_team_scope", "oj_team", ["scope"])
    op.create_index("ix_oj_team_course_id", "oj_team", ["course_id"])
    op.create_index("ix_oj_team_owner_id", "oj_team", ["owner_id"])
    op.create_index("ix_oj_team_status", "oj_team", ["status"])

    op.create_table(
        "oj_team_member",
        sa.Column("id", sa.String(length=64), primary_key=True, comment="主键"),
        sa.Column("team_id", sa.String(length=64), nullable=False, comment="小组ID"),
        sa.Column("account_id", sa.String(length=64), nullable=False, comment="Portal 账户ID"),
        sa.Column("role", sa.String(length=16), nullable=False, server_default="MEMBER", comment="OWNER|ADMIN|MEMBER"),
        sa.Column("joined_at", sa.DateTime(timezone=True), nullable=False, comment="加入时间"),
        sa.Column("left_at", sa.DateTime(timezone=True), nullable=True, comment="离开时间"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.UniqueConstraint("team_id", "account_id", name="uq_oj_team_member_team_account"),
    )
    op.create_index("ix_oj_team_member_team_id", "oj_team_member", ["team_id"])
    op.create_index("ix_oj_team_member_account_id", "oj_team_member", ["account_id"])


def downgrade() -> None:
    op.drop_table("oj_team_member")
    op.drop_table("oj_team")
    op.drop_table("oj_course_task_submission")
    op.drop_table("oj_course_task_progress")
    op.drop_table("oj_course_task_problem")
    op.drop_table("oj_course_task")
    op.drop_table("oj_course_announcement")
    op.drop_table("oj_course")
    op.drop_table("oj_class_member")
    op.drop_table("oj_class")
