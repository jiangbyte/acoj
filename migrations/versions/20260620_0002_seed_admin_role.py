"""seed admin account and role"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "20260620_0002"
down_revision: str | Sequence[str] | None = "20260620_0001"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None

ADMIN_ACCOUNT_ID = "seed_admin_account"
SUPER_ADMIN_ROLE_ID = "seed_role_super_admin"
ADMIN_ROLE_REL_ID = "seed_rel_admin_super_admin"
ADMIN_PASSWORD_HASH = (
    "$pbkdf2-sha256$29000$9F4rZWxtTQkBoHROKWUsRQ$"
    "gTUk2O4CMqpmvYVGc5e9.SuCERJnkSefgRbjNtJEfpE"
)


def upgrade() -> None:
    account_table = sa.table(
        "sys_account",
        sa.column("id", sa.String),
        sa.column("account", sa.String),
        sa.column("password_hash", sa.String),
        sa.column("account_type", sa.String),
        sa.column("account_status", sa.String),
        sa.column("name", sa.String),
        sa.column("nickname", sa.String),
        sa.column("phone", sa.String),
        sa.column("email", sa.String),
        sa.column("is_superuser", sa.Boolean),
    )
    admin_profile_table = sa.table(
        "admin_user_profile",
        sa.column("account_id", sa.String),
        sa.column("real_name", sa.String),
        sa.column("title", sa.String),
    )
    account_role_table = sa.table(
        "sys_account_role_rel",
        sa.column("id", sa.String),
        sa.column("account_id", sa.String),
        sa.column("role_id", sa.String),
    )

    op.bulk_insert(
        account_table,
        [
            {
                "id": ADMIN_ACCOUNT_ID,
                "account": "admin",
                "password_hash": ADMIN_PASSWORD_HASH,
                "account_type": "ADMIN",
                "account_status": "ENABLED",
                "name": "系统管理员",
                "nickname": "Admin",
                "phone": "13800000001",
                "email": "admin@example.com",
                "is_superuser": True,
            }
        ],
    )
    op.bulk_insert(
        admin_profile_table,
        [
            {
                "account_id": ADMIN_ACCOUNT_ID,
                "real_name": "系统管理员",
                "title": "超级管理员",
            }
        ],
    )
    op.execute(
        sa.text(
            """
            insert into sys_role (
                id, code, name, category, scope_type, sort, status,
                is_builtin, description, extra
            )
            values (
                :id, :code, :name, :category, :scope_type, :sort, :status,
                :is_builtin, :description, '{}'::json
            )
            """
        ).bindparams(
            id=SUPER_ADMIN_ROLE_ID,
            code="super_admin",
            name="超级管理员",
            category="system",
            scope_type="PLATFORM",
            sort=1,
            status="ENABLED",
            is_builtin=True,
            description="系统内置超级管理员角色",
        )
    )
    op.bulk_insert(
        account_role_table,
        [
            {
                "id": ADMIN_ROLE_REL_ID,
                "account_id": ADMIN_ACCOUNT_ID,
                "role_id": SUPER_ADMIN_ROLE_ID,
            }
        ],
    )


def downgrade() -> None:
    op.execute(
        sa.text("delete from sys_account_role_rel where id = :id").bindparams(
            id=ADMIN_ROLE_REL_ID
        )
    )
    op.execute(
        sa.text("delete from admin_user_profile where account_id = :account_id").bindparams(
            account_id=ADMIN_ACCOUNT_ID
        )
    )
    op.execute(
        sa.text("delete from sys_role where id = :id").bindparams(id=SUPER_ADMIN_ROLE_ID)
    )
    op.execute(
        sa.text("delete from sys_account where id = :id").bindparams(id=ADMIN_ACCOUNT_ID)
    )
