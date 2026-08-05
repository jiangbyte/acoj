"""Course-class many-to-many binding (SHARED / PER_CLASS)."""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "l9m0n1o2p3q4"
down_revision: str | Sequence[str] | None = "k8l9m0n1o2p3"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table(
        "oj_course_class",
        sa.Column("id", sa.String(length=64), primary_key=True),
        sa.Column("course_id", sa.String(length=64), nullable=False, comment="课程ID"),
        sa.Column("class_id", sa.String(length=64), nullable=False, comment="班级ID"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("created_by", sa.String(length=64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_by", sa.String(length=64), nullable=True),
        sa.UniqueConstraint("course_id", "class_id", name="uq_oj_course_class_course_class"),
    )
    op.create_index("ix_oj_course_class_course_id", "oj_course_class", ["course_id"])
    op.create_index("ix_oj_course_class_class_id", "oj_course_class", ["class_id"])

    op.add_column(
        "oj_course",
        sa.Column(
            "binding_mode",
            sa.String(length=16),
            nullable=False,
            server_default="PER_CLASS",
            comment="SHARED|PER_CLASS",
        ),
    )

    # Migrate legacy oj_course.class_id → oj_course_class
    op.execute(
        """
        INSERT INTO oj_course_class (id, course_id, class_id, created_at, updated_at)
        SELECT c.id || '-bind', c.id, c.class_id, COALESCE(c.created_at, now()), COALESCE(c.updated_at, now())
        FROM oj_course c
        WHERE c.class_id IS NOT NULL
          AND NOT EXISTS (
            SELECT 1 FROM oj_course_class x WHERE x.course_id = c.id AND x.class_id = c.class_id
          )
        """
    )

    op.drop_index("ix_oj_course_class_status", table_name="oj_course")
    op.drop_index("ix_oj_course_class_id", table_name="oj_course")
    op.drop_column("oj_course", "class_id")
    op.create_index("ix_oj_course_status", "oj_course", ["status"])


def downgrade() -> None:
    op.add_column(
        "oj_course",
        sa.Column("class_id", sa.String(length=64), nullable=True, comment="班级ID"),
    )
    op.execute(
        """
        UPDATE oj_course c
        SET class_id = sub.class_id
        FROM (
            SELECT DISTINCT ON (course_id) course_id, class_id
            FROM oj_course_class
            ORDER BY course_id, id
        ) sub
        WHERE c.id = sub.course_id
        """
    )
    op.execute("UPDATE oj_course SET class_id = '' WHERE class_id IS NULL")
    op.alter_column("oj_course", "class_id", nullable=False)
    op.create_index("ix_oj_course_class_id", "oj_course", ["class_id"])
    op.create_index("ix_oj_course_class_status", "oj_course", ["class_id", "status"])

    op.drop_index("ix_oj_course_status", table_name="oj_course")
    op.drop_column("oj_course", "binding_mode")
    op.drop_index("ix_oj_course_class_class_id", table_name="oj_course_class")
    op.drop_index("ix_oj_course_class_course_id", table_name="oj_course_class")
    op.drop_table("oj_course_class")
