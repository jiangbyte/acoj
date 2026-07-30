"""store SPJ/interactor source as Text instead of storage keys"""

from collections.abc import Sequence
from typing import Any

import sqlalchemy as sa
from alembic import op

revision: str = "c4d5e6f7a809"
down_revision: str | Sequence[str] | None = "b2c3d4e5f607"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def _try_load_source(object_name: str | None) -> str | None:
    if not object_name or not str(object_name).strip():
        return None
    try:
        from app.modules.biz.problem.judge_bridge import read_storage_bytes

        return read_storage_bytes(str(object_name)).decode("utf-8", errors="replace")
    except Exception:
        return None


def upgrade() -> None:
    op.add_column(
        "oj_problem_data",
        sa.Column("spj_source", sa.Text(), nullable=True, comment="SPJ 源码（C++17）"),
    )
    op.add_column(
        "oj_problem_data",
        sa.Column("interactor_source", sa.Text(), nullable=True, comment="交互器源码（C++17）"),
    )

    connection = op.get_bind()
    rows = connection.execute(
        sa.text(
            "SELECT id, spj_file_id, interactor_file_id FROM oj_problem_data"
        )
    ).mappings().all()
    for row in rows:
        updates: dict[str, Any] = {
            "spj_source": _try_load_source(row["spj_file_id"]),
            "interactor_source": _try_load_source(row["interactor_file_id"]),
        }
        if updates["spj_source"] is None and updates["interactor_source"] is None:
            continue
        connection.execute(
            sa.text(
                """
                UPDATE oj_problem_data
                SET spj_source = :spj_source,
                    interactor_source = :interactor_source
                WHERE id = :id
                """
            ),
            {"id": row["id"], **updates},
        )

    op.drop_column("oj_problem_data", "spj_file_id")
    op.drop_column("oj_problem_data", "interactor_file_id")


def downgrade() -> None:
    op.add_column(
        "oj_problem_data",
        sa.Column(
            "spj_file_id",
            sa.String(length=512),
            nullable=True,
            comment="自定义 SPJ storage key",
        ),
    )
    op.add_column(
        "oj_problem_data",
        sa.Column(
            "interactor_file_id",
            sa.String(length=512),
            nullable=True,
            comment="交互器 storage key",
        ),
    )
    op.drop_column("oj_problem_data", "spj_source")
    op.drop_column("oj_problem_data", "interactor_source")
