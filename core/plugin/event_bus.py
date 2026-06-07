"""
Simple asynchronous event bus for cross-plugin communication.

Usage::

    from core.plugin import subscribe, publish


    @subscribe("user.created")
    async def on_user_created(user_id: str, **data):
        print(f"User created: {user_id}")


    # somewhere else
    await publish("user.created", user_id="abc123", extra={})
"""

from __future__ import annotations

import inspect
import logging
from typing import Any, Awaitable, Callable

logger = logging.getLogger(__name__)

_subscribers: dict[str, list[Callable[..., Awaitable[None]]]] = {}


def subscribe(topic: str) -> Callable:
    """Decorator that registers an ``async`` function as a subscriber
    to the given topic."""

    def decorator(func: Callable[..., Awaitable[None]]) -> Callable[..., Awaitable[None]]:
        if not inspect.iscoroutinefunction(func):
            raise TypeError(f"{func.__name__} must be async to be used with @subscribe")
        _subscribers.setdefault(topic, []).append(func)
        logger.debug("Subscriber %s registered for topic '%s'", func.__name__, topic)
        return func

    return decorator


async def publish(topic: str, **data: Any) -> None:
    """Publish an event to all subscribers of *topic*.

    Every subscriber receives the keyword arguments provided here.
    Exceptions from individual subscribers are logged but do **not**
    prevent other subscribers from running.
    """
    for handler in _subscribers.get(topic, []):
        try:
            await handler(**data)
        except Exception:
            logger.exception("Subscriber %s failed for topic '%s'", handler.__name__, topic)


class EventBus:
    """Alternative class-based API for the event bus."""

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
