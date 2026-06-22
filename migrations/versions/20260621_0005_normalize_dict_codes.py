"""normalize dict code format"""

from collections.abc import Sequence

revision: str = "20260621_0005"
down_revision: str | Sequence[str] | None = "20260621_0004"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    pass


def downgrade() -> None:
    pass
