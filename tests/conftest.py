from __future__ import annotations

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


class FakeRedis:
    def __init__(self) -> None:
        self.values: dict[str, object] = {}
        self.sets: dict[str, set[object]] = {}
        self.hashes: dict[str, dict[object, str]] = {}

    async def setex(self, key: object, ttl: int, value: object) -> None:
        self.values[str(key)] = value

    async def set(self, key: object, value: object) -> None:
        self.values[str(key)] = value

    async def get(self, key: object) -> object | None:
        return self.values.get(str(key))

    async def delete(self, key: object) -> None:
        self.values.pop(str(key), None)

    async def sadd(self, key: object, *values: object) -> None:
        self.sets.setdefault(str(key), set()).update(values)

    async def smembers(self, key: object) -> set[object]:
        return set(self.sets.get(str(key), set()))

    async def srem(self, key: object, *values: object) -> None:
        existing = self.sets.get(str(key))
        if existing is None:
            return
        for value in values:
            existing.discard(value)

    async def hincrby(self, key: object, field: object, amount: int) -> int:
        hash_key = str(key)
        current = int(self.hashes.setdefault(hash_key, {}).get(field, "0"))
        next_value = current + amount
        self.hashes[hash_key][field] = str(next_value)
        return next_value

    async def hgetall(self, key: object) -> dict[object, str]:
        return dict(self.hashes.get(str(key), {}))

    async def hdel(self, key: object, *fields: object) -> int:
        existing = self.hashes.get(str(key), {})
        removed = 0
        for field in fields:
            if field in existing:
                removed += 1
                del existing[field]
        return removed

    async def ping(self) -> bool:
        return True

    async def aclose(self) -> None:
        return None


@pytest.fixture(autouse=True)
def fake_redis(monkeypatch) -> FakeRedis:
    from app.platform.cache import redis as redis_module

    fake = FakeRedis()
    monkeypatch.setattr(redis_module, "redis_client", fake)
    yield fake


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
