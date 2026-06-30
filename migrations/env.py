import asyncio
from logging.config import fileConfig

from alembic import context
from sqlalchemy import pool
from sqlalchemy.engine import Connection
from sqlalchemy.ext.asyncio import async_engine_from_config

from app.core.config.settings import settings
from app.modules.iam.account import model as _iam_account_model  # noqa: F401
from app.modules.iam.dept import model as _iam_dept_model  # noqa: F401
from app.modules.iam.grant import model as _iam_grant_model  # noqa: F401
from app.modules.iam.group import model as _iam_group_model  # noqa: F401
from app.modules.iam.position import model as _iam_position_model  # noqa: F401
from app.modules.iam.resource import model as _iam_resource_model  # noqa: F401
from app.modules.iam.role import model as _iam_role_model  # noqa: F401
from app.modules.message.message import model as _message_model  # noqa: F401
from app.modules.message.notification import model as _notification_model  # noqa: F401
from app.modules.message.todo import model as _todo_model  # noqa: F401
from app.modules.sys.banner import model as _banner_model  # noqa: F401
from app.modules.sys.dict import model as _dict_model  # noqa: F401
from app.modules.sys.file import model as _file_model  # noqa: F401
from app.modules.user.admin import model as _admin_profile_model  # noqa: F401
from app.modules.user.portal import model as _portal_profile_model  # noqa: F401
from app.platform.db.base import Base

config = context.config
config.set_main_option("sqlalchemy.url", settings.db.url)

if config.config_file_name is not None:
    fileConfig(config.config_file_name)

target_metadata = Base.metadata


def include_name(name: str | None, type_: str, parent_names: dict[str, str | None]) -> bool:
    """Keep autogenerate scoped to tables declared in project metadata."""

    if type_ == "schema":
        return name in (None, target_metadata.schema)
    if type_ == "table":
        return name in target_metadata.tables
    return True


def run_migrations_offline() -> None:
    context.configure(
        url=settings.db.url,
        target_metadata=target_metadata,
        compare_type=True,
        include_name=include_name,
        literal_binds=True,
        dialect_opts={"paramstyle": "named"},
    )

    with context.begin_transaction():
        context.run_migrations()


def do_run_migrations(connection: Connection) -> None:
    context.configure(
        connection=connection,
        target_metadata=target_metadata,
        compare_type=True,
        include_name=include_name,
    )

    with context.begin_transaction():
        context.run_migrations()


async def run_async_migrations() -> None:
    connectable = async_engine_from_config(
        config.get_section(config.config_ini_section, {}),
        prefix="sqlalchemy.",
        poolclass=pool.NullPool,
    )

    async with connectable.connect() as connection:
        await connection.run_sync(do_run_migrations)

    await connectable.dispose()


def run_migrations_online() -> None:
    asyncio.run(run_async_migrations())


if context.is_offline_mode():
    run_migrations_offline()
else:
    run_migrations_online()
