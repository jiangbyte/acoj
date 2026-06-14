from __future__ import annotations

import logging
from typing import Optional

import redis.asyncio as redis

from sdk.config.settings import settings

logger = logging.getLogger(__name__)


class RedisRuntime:
    def __init__(self) -> None:
        self._client: Optional[redis.Redis] = None

    async def init(self) -> None:
        self._client = redis.from_url(
            settings.redis.url,
            decode_responses=True,
            max_connections=settings.redis.max_connections,
            socket_connect_timeout=settings.redis.socket_connect_timeout,
            socket_timeout=settings.redis.socket_timeout,
            retry_on_timeout=settings.redis.retry_on_timeout,
            health_check_interval=settings.redis.health_check_interval,
        )
        try:
            await self._client.ping()
            logger.info("[Database] Redis connection verified")
        except Exception as exc:
            logger.critical("[Database] Redis connection failed: %s", exc)
            await self._client.close()
            self._client = None
            raise

    def get_client(self) -> Optional[redis.Redis]:
        return self._client

    async def close(self) -> None:
        if self._client is not None:
            await self._client.close()
            self._client = None
            logger.info("[Database] Redis connection closed")


runtime = RedisRuntime()


async def init() -> None:
    await runtime.init()


def get_client() -> Optional[redis.Redis]:
    return runtime.get_client()


async def close() -> None:
    await runtime.close()
