from __future__ import annotations

import asyncio
import logging

from sqlalchemy import create_engine, select
from sqlalchemy.engine import Engine
from sqlalchemy.orm import Session, sessionmaker

from sdk.config.settings import settings

logger = logging.getLogger(__name__)


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

    def get_db(self):
        db = self.create_session()
        try:
            yield db
        finally:
            db.close()

    async def verify_connection(self) -> None:
        def _sync_check() -> None:
            db = self.create_session()
            try:
                db.execute(select(1))
            finally:
                db.close()

        try:
            await asyncio.to_thread(_sync_check)
            logger.info("[Database] MySQL connection verified")
        except Exception as exc:
            logger.critical("[Database] MySQL connection failed: %s", exc)
            raise

    def dispose(self) -> None:
        self._engine.dispose()
        logger.info("[Database] MySQL connection disposed")


runtime = DatabaseRuntime()
engine = runtime.engine
SessionLocal = runtime.session_factory


def create_session() -> Session:
    return runtime.create_session()


def get_db():
    yield from runtime.get_db()


async def verify_connection() -> None:
    await runtime.verify_connection()


def dispose() -> None:
    runtime.dispose()
