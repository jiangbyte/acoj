"""Problem list, learning plan, daily problem tables."""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "f3a4b5c6d7e8"
down_revision: str | Sequence[str] | None = "e2f3a4b5c6d7"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table(
        "oj_problem_list",
        sa.Column("id", sa.String(length=64), primary_key=True, comment="主键"),
        sa.Column("kind", sa.String(length=16), nullable=False, comment="PERSONAL|OFFICIAL"),
        sa.Column("owner_id", sa.String(length=64), nullable=True, comment="个人题单所有者"),
        sa.Column("code", sa.String(length=64), nullable=True, comment="官方题单编码"),
        sa.Column("title", sa.String(length=200), nullable=False, comment="标题"),
        sa.Column("summary", sa.Text(), nullable=True, comment="摘要"),
        sa.Column("cover_url", sa.String(length=512), nullable=True, comment="封面"),
        sa.Column("visibility", sa.String(length=16), nullable=False, comment="PRIVATE|PUBLIC"),
        sa.Column("is_system", sa.Boolean(), nullable=False, server_default=sa.text("false"), comment="系统题单（收藏）"),
        sa.Column("status", sa.String(length=16), nullable=False, server_default="ENABLED", comment="状态"),
        sa.Column("sort", sa.Integer(), nullable=False, server_default="0", comment="排序"),
        sa.Column("extra", sa.JSON(), nullable=False, server_default=sa.text("'{}'::json"), comment="扩展"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
    )
    op.create_index("ix_oj_problem_list_owner_id", "oj_problem_list", ["owner_id"])
    op.create_index("ix_oj_problem_list_kind_status", "oj_problem_list", ["kind", "status"])
    op.create_index("ix_oj_problem_list_code", "oj_problem_list", ["code"], unique=False)

    op.create_table(
        "oj_problem_list_item",
        sa.Column("id", sa.String(length=64), primary_key=True, comment="主键"),
        sa.Column("list_id", sa.String(length=64), nullable=False, comment="题单ID"),
        sa.Column("problem_id", sa.String(length=64), nullable=False, comment="题目ID"),
        sa.Column("sort", sa.Integer(), nullable=False, server_default="0", comment="排序"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.UniqueConstraint("list_id", "problem_id", name="uq_oj_problem_list_item_list_problem"),
    )
    op.create_index("ix_oj_problem_list_item_list_id", "oj_problem_list_item", ["list_id"])
    op.create_index("ix_oj_problem_list_item_problem_id", "oj_problem_list_item", ["problem_id"])

    op.create_table(
        "oj_learning_plan",
        sa.Column("id", sa.String(length=64), primary_key=True, comment="主键"),
        sa.Column("code", sa.String(length=64), nullable=False, comment="计划编码"),
        sa.Column("title", sa.String(length=200), nullable=False, comment="标题"),
        sa.Column("subtitle", sa.String(length=255), nullable=True, comment="副标题"),
        sa.Column("overview", sa.Text(), nullable=True, comment="概述"),
        sa.Column("cover_url", sa.String(length=512), nullable=True, comment="封面"),
        sa.Column("category", sa.String(length=32), nullable=False, comment="FEATURED|INTERVIEW"),
        sa.Column("status", sa.String(length=16), nullable=False, server_default="ENABLED", comment="状态"),
        sa.Column("sort", sa.Integer(), nullable=False, server_default="0", comment="排序"),
        sa.Column("extra", sa.JSON(), nullable=False, server_default=sa.text("'{}'::json"), comment="扩展"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.UniqueConstraint("code", name="uq_oj_learning_plan_code"),
    )
    op.create_index("ix_oj_learning_plan_category_status", "oj_learning_plan", ["category", "status"])

    op.create_table(
        "oj_learning_plan_section",
        sa.Column("id", sa.String(length=64), primary_key=True, comment="主键"),
        sa.Column("plan_id", sa.String(length=64), nullable=False, comment="计划ID"),
        sa.Column("title", sa.String(length=200), nullable=False, comment="分组标题"),
        sa.Column("sort", sa.Integer(), nullable=False, server_default="0", comment="排序"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
    )
    op.create_index("ix_oj_learning_plan_section_plan_id", "oj_learning_plan_section", ["plan_id"])

    op.create_table(
        "oj_learning_plan_item",
        sa.Column("id", sa.String(length=64), primary_key=True, comment="主键"),
        sa.Column("section_id", sa.String(length=64), nullable=False, comment="分组ID"),
        sa.Column("problem_id", sa.String(length=64), nullable=False, comment="题目ID"),
        sa.Column("sort", sa.Integer(), nullable=False, server_default="0", comment="排序"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.UniqueConstraint("section_id", "problem_id", name="uq_oj_learning_plan_item_section_problem"),
    )
    op.create_index("ix_oj_learning_plan_item_section_id", "oj_learning_plan_item", ["section_id"])
    op.create_index("ix_oj_learning_plan_item_problem_id", "oj_learning_plan_item", ["problem_id"])

    op.create_table(
        "oj_daily_problem",
        sa.Column("id", sa.String(length=64), primary_key=True, comment="主键"),
        sa.Column("day_date", sa.Date(), nullable=False, comment="日期（上海时区日历日）"),
        sa.Column("problem_id", sa.String(length=64), nullable=False, comment="题目ID"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.UniqueConstraint("day_date", name="uq_oj_daily_problem_day_date"),
    )
    op.create_index("ix_oj_daily_problem_problem_id", "oj_daily_problem", ["problem_id"])


def downgrade() -> None:
    op.drop_table("oj_daily_problem")
    op.drop_table("oj_learning_plan_item")
    op.drop_table("oj_learning_plan_section")
    op.drop_table("oj_learning_plan")
    op.drop_table("oj_problem_list_item")
    op.drop_table("oj_problem_list")
