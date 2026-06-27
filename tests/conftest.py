import asyncio
from collections.abc import AsyncIterator

import pytest
from fastapi import APIRouter
from httpx import ASGITransport, AsyncClient
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine

from app.core.config.settings import settings
from app.deps.db import get_db_session
from app.factory import create_app
from app.platform.db.base import Base


@pytest.fixture(scope="session")
def event_loop():
    loop = asyncio.new_event_loop()
    yield loop
    loop.close()


@pytest.fixture
async def db_session() -> AsyncIterator[AsyncSession]:
    engine = create_async_engine("sqlite+aiosqlite:///:memory:")
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    session_factory = async_sessionmaker(engine, expire_on_commit=False)
    async with session_factory() as session:
        yield session
    await engine.dispose()


@pytest.fixture
async def client() -> AsyncIterator[AsyncClient]:
    app = create_app()
    test_router = APIRouter()

    @test_router.get("/__test/error")
    async def raise_test_error() -> None:
        """测试专用异常路由，用于验证未处理异常能否被统一包装。"""
        raise RuntimeError("test unhandled error")

    app.include_router(test_router)
    engine = create_async_engine("sqlite+aiosqlite:///:memory:")
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    session_factory = async_sessionmaker(engine, expire_on_commit=False)

    async def override_get_db_session() -> AsyncIterator[AsyncSession]:
        async with session_factory() as session:
            yield session

    app.dependency_overrides[get_db_session] = override_get_db_session
    async with AsyncClient(
        transport=ASGITransport(app=app, raise_app_exceptions=False),
        base_url="http://testserver",
    ) as ac:
        try:
            yield ac
        finally:
            app.dependency_overrides.clear()
            await engine.dispose()


@pytest.fixture
async def metrics_client() -> AsyncIterator[AsyncClient]:
    old_enabled = settings.observability.enabled
    old_metrics = settings.observability.metrics_enabled
    old_path = settings.observability.metrics_path
    settings.observability.enabled = True
    settings.observability.metrics_enabled = True
    settings.observability.metrics_path = "/metrics"
    app = create_app()
    try:
        async with AsyncClient(
            transport=ASGITransport(app=app, raise_app_exceptions=False),
            base_url="http://testserver",
        ) as ac:
            yield ac
    finally:
        settings.observability.enabled = old_enabled
        settings.observability.metrics_enabled = old_metrics
        settings.observability.metrics_path = old_path
