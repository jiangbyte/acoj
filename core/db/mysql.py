import asyncio
import logging
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker

from config.settings import settings

logger = logging.getLogger(__name__)

engine = create_engine(
    settings.db.url,
    pool_size=settings.db.pool_size,
    max_overflow=settings.db.max_overflow,
    pool_recycle=settings.db.pool_recycle,
    pool_pre_ping=settings.db.pool_pre_ping,
    pool_timeout=settings.db.pool_timeout,
    connect_args={"connect_timeout": settings.db.connect_timeout},
    echo=settings.db.echo,
)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


async def verify_connection():
    """Verify MySQL connectivity. Raises on failure, stopping app startup."""
    def _sync_check():
        db = SessionLocal()
        try:
            db.execute(text("SELECT 1"))
        finally:
            db.close()

    try:
        await asyncio.to_thread(_sync_check)
        logger.info("[Database] MySQL connection verified")
    except Exception as e:
        logger.critical("[Database] MySQL connection failed: %s", e)
        raise


def dispose():
    engine.dispose()
    logger.info("[Database] MySQL connection disposed")
