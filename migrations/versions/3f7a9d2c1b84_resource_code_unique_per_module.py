"""resource code unique per module"""

from collections.abc import Sequence

from alembic import op


revision: str = "3f7a9d2c1b84"
down_revision: str | Sequence[str] | None = "c12e5b0797c1"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.drop_constraint("uq_sys_resource_code", "sys_resource", type_="unique")
    op.create_unique_constraint(
        "uq_sys_resource_module_id_code",
        "sys_resource",
        ["module_id", "code"],
    )


def downgrade() -> None:
    op.drop_constraint("uq_sys_resource_module_id_code", "sys_resource", type_="unique")
    op.create_unique_constraint("uq_sys_resource_code", "sys_resource", ["code"])
