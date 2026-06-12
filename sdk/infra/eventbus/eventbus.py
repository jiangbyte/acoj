from __future__ import annotations

import inspect
import logging
from typing import Any, Awaitable, Callable

logger = logging.getLogger(__name__)

_subscribers: dict[str, list[Callable[..., Awaitable[None]]]] = {}


def subscribe(topic: str) -> Callable:
    def decorator(func: Callable[..., Awaitable[None]]) -> Callable[..., Awaitable[None]]:
        if not inspect.iscoroutinefunction(func):
            raise TypeError(f"{func.__name__} must be async to be used with @subscribe")
        _subscribers.setdefault(topic, []).append(func)
        logger.debug("Subscriber %s registered for topic '%s'", func.__name__, topic)
        return func

    return decorator


async def publish(topic: str, **data: Any) -> None:
    for handler in _subscribers.get(topic, []):
        try:
            await handler(**data)
        except Exception:
            logger.exception("Subscriber %s failed for topic '%s'", handler.__name__, topic)


class EventBus:
    @staticmethod
    def subscribe(topic: str) -> Callable:
        return subscribe(topic)

    @staticmethod
    async def publish(topic: str, **data: Any) -> None:
        await publish(topic, **data)

    @staticmethod
    def subscribers(topic: str | None = None) -> dict[str, list[Callable]]:
        if topic:
            return {topic: list(_subscribers.get(topic, []))}
        return dict(_subscribers)
