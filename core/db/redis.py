from typing import Optional
import logging
import redis.asyncio as redis

from config.settings import settings

logger = logging.getLogger(__name__)

_client: Optional[redis.Redis] = None


async def init():
    global _client
    _client = redis.from_url(
        settings.redis.url,
        decode_responses=True,
        max_connections=settings.redis.max_connections,
        socket_connect_timeout=settings.redis.socket_connect_timeout,
        socket_timeout=settings.redis.socket_timeout,
        retry_on_timeout=settings.redis.retry_on_timeout,
        health_check_interval=settings.redis.health_check_interval,
    )
    try:
        await _client.ping()
        logger.info("[Database] Redis connection verified")
    except Exception as e:
        logger.critical("[Database] Redis connection failed: %s", e)
        await _client.close()
        _client = None
        raise


def get_client() -> Optional[redis.Redis]:
    return _client


async def close():
    global _client
    if _client:
        await _client.close()
        _client = None
        logger.info("[Database] Redis connection closed")
