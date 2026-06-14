from __future__ import annotations

import asyncio
import logging
from collections.abc import AsyncGenerator

from sqlalchemy import create_engine, select
from sqlalchemy.engine import Engine
from sqlalchemy.ext.asyncio import (
    AsyncEngine,
    AsyncSession,
    async_sessionmaker,
    create_async_engine,
)
from sqlalchemy.orm import Session, sessionmaker

from sdk.config.settings import settings

logger = logging.getLogger(__name__)


# ═════════════════════════════════════════════════════════════════════
# Async engine — the request path (handler → service → repository).
# ═════════════════════════════════════════════════════════════════════

class AsyncDatabaseRuntime:
    def __init__(self) -> None:
        self._engine: AsyncEngine = create_async_engine(
            settings.db.async_url,
            pool_size=settings.db.pool_size,
            max_overflow=settings.db.max_overflow,
            pool_recycle=settings.db.pool_recycle,
            pool_pre_ping=settings.db.pool_pre_ping,
            pool_timeout=settings.db.pool_timeout,
            connect_args={"connect_timeout": settings.db.connect_timeout},
            echo=settings.db.echo,
        )
        self._session_factory: async_sessionmaker[AsyncSession] = async_sessionmaker(
            bind=self._engine,
            expire_on_commit=False,
            autoflush=False,
        )

    @property
    def engine(self) -> AsyncEngine:
        return self._engine

    @property
    def session_factory(self) -> async_sessionmaker[AsyncSession]:
        return self._session_factory

    async def get_db(self) -> AsyncGenerator[AsyncSession, None]:
        async with self._session_factory() as session:
            yield session

    async def verify_connection(self) -> None:
        try:
            async with self._engine.connect() as conn:
                await conn.execute(select(1))
            logger.info("[Database] MySQL (async) connection verified")
        except Exception as exc:
            logger.critical("[Database] MySQL (async) connection failed: %s", exc)
            raise

    async def dispose(self) -> None:
        await self._engine.dispose()
        logger.info("[Database] MySQL (async) connection disposed")


# ═════════════════════════════════════════════════════════════════════
# Sync engine — retained for off-event-loop code only:
#   * CLI tooling (cli/migrate.py, cli/codegen.py)
#   * seeds (seed_admin_user / run_seeds)
#   * the log persister worker thread (DbLogPersister)
# Never used on the event loop.
# ═════════════════════════════════════════════════════════════════════

class DatabaseRuntime:
    def __init__(self) -> None:
        self._engine: Engine = create_engine(
            settings.db.url,
            pool_size=settings.db.pool_size,
            max_overflow=settings.db.max_overflow,
            pool_recycle=settings.db.pool_recycle,
            pool_pre_ping=settings.db.pool_pre_ping,
            pool_timeout=settings.db.pool_timeout,
            connect_args={"connect_timeout": settings.db.connect_timeout},
            echo=settings.db.echo,
        )
        self._session_factory = sessionmaker(autocommit=False, autoflush=False, bind=self._engine)

    @property
    def engine(self) -> Engine:
        return self._engine

    @property
    def session_factory(self):
        return self._session_factory

    def create_session(self) -> Session:
        return self._session_factory()

    async def verify_connection(self) -> None:
        def _sync_check() -> None:
            db = self.create_session()
            try:
                db.execute(select(1))
            finally:
                db.close()

        try:
            await asyncio.to_thread(_sync_check)
            logger.info("[Database] MySQL (sync) connection verified")
        except Exception as exc:
            logger.critical("[Database] MySQL (sync) connection failed: %s", exc)
            raise

    def dispose(self) -> None:
        self._engine.dispose()
        logger.info("[Database] MySQL (sync) connection disposed")


# ── Async runtime (primary, request path) ────────────────────────────
async_runtime = AsyncDatabaseRuntime()
async_engine = async_runtime.engine
AsyncSessionLocal = async_runtime.session_factory


async def get_db() -> AsyncGenerator[AsyncSession, None]:
    async for session in async_runtime.get_db():
        yield session


async def verify_connection() -> None:
    await async_runtime.verify_connection()


async def dispose() -> None:
    await async_runtime.dispose()


# ── Sync runtime (off-event-loop: CLI, seeds, log persister) ─────────
runtime = DatabaseRuntime()
engine = runtime.engine
SessionLocal = runtime.session_factory


def create_session() -> Session:
    return runtime.create_session()


async def verify_connection_sync() -> None:
    await runtime.verify_connection()


def dispose_sync() -> None:
    runtime.dispose()
