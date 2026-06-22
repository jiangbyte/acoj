"""update seed admin password to bcrypt"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "20260620_0003"
down_revision: str | Sequence[str] | None = "20260620_0002"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None

ADMIN_ACCOUNT_ID = "seed_admin_account"
BCRYPT_ADMIN_PASSWORD_HASH = "$2b$12$DVJTqqybLBD2k37Ztpwuqu7LTJOBTQtcevDV0Zb9xfPP.xriYrDI."
PBKDF2_ADMIN_PASSWORD_HASH = (
    "$pbkdf2-sha256$29000$9F4rZWxtTQkBoHROKWUsRQ$"
    "gTUk2O4CMqpmvYVGc5e9.SuCERJnkSefgRbjNtJEfpE"
)


def upgrade() -> None:
    op.execute(
        sa.text("update sys_account set password_hash = :password_hash where id = :id").bindparams(
            password_hash=BCRYPT_ADMIN_PASSWORD_HASH,
            id=ADMIN_ACCOUNT_ID,
        )
    )


def downgrade() -> None:
    op.execute(
        sa.text("update sys_account set password_hash = :password_hash where id = :id").bindparams(
            password_hash=PBKDF2_ADMIN_PASSWORD_HASH,
            id=ADMIN_ACCOUNT_ID,
        )
    )
