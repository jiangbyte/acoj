from __future__ import annotations

import asyncio
import inspect
import logging
from dataclasses import dataclass
from itertools import count
from typing import Any, Callable

logger = logging.getLogger(__name__)

MAX_CONCURRENT_SUBSCRIBERS = 256

TopicUserConnected = "user:connected"
TopicUserDisconnected = "user:disconnected"
TopicMessageSent = "message:sent"
TopicMessageRead = "message:read"


@dataclass(slots=True)
class Event:
    topic: str
    data: Any


@dataclass(slots=True)
class _SubscriberEntry:
    id: int
    fn: Callable[[Event], Any]


class _Bus:
    def __init__(self) -> None:
        self._subscribers: dict[str, list[_SubscriberEntry]] = {}
        self._ids = count(1)
        self._semaphore = asyncio.Semaphore(MAX_CONCURRENT_SUBSCRIBERS)

    async def publish(self, topic: str, data: Any = None) -> None:
        subscribers = list(self._subscribers.get(topic, ()))
        if not subscribers:
            return

        event = Event(topic=topic, data=data)
        await asyncio.gather(*(self._run(entry.fn, event) for entry in subscribers), return_exceptions=True)

    def subscribe(self, topic: str, sub: Callable[[Event], Any]) -> Callable[[], None]:
        entry = _SubscriberEntry(next(self._ids), sub)
        self._subscribers.setdefault(topic, []).append(entry)

        def unsubscribe() -> None:
            subscribers = self._subscribers.get(topic, [])
            self._subscribers[topic] = [item for item in subscribers if item.id != entry.id]

        return unsubscribe

    async def _run(self, fn: Callable[[Event], Any], event: Event) -> None:
        async with self._semaphore:
            try:
                result = fn(event)
                if inspect.isawaitable(result):
                    await result
            except Exception:
                logger.exception("event subscriber failed: topic=%s subscriber=%s", event.topic, getattr(fn, "__name__", repr(fn)))

    def snapshot(self, topic: str | None = None) -> dict[str, list[Callable[[Event], Any]]]:
        if topic is not None:
            return {topic: [entry.fn for entry in self._subscribers.get(topic, [])]}
        return {name: [entry.fn for entry in entries] for name, entries in self._subscribers.items()}


DefaultBus = _Bus()


def subscribe(topic: str, sub: Callable[[Event], Any] | None = None):
    if sub is not None:
        return DefaultBus.subscribe(topic, sub)

    def decorator(fn: Callable[[Event], Any]) -> Callable[[Event], Any]:
        DefaultBus.subscribe(topic, fn)
        return fn

    return decorator


async def publish(topic: str, data: Any = None, **kwargs: Any) -> None:
    payload = data if data is not None else kwargs
    await DefaultBus.publish(topic, payload)


class EventBus:
    @staticmethod
    def publish(topic: str, data: Any = None) -> Any:
        return DefaultBus.publish(topic, data)

    @staticmethod
    def subscribe(topic: str, sub: Callable[[Event], Any]) -> Callable[[], None]:
        return DefaultBus.subscribe(topic, sub)

    @staticmethod
    def snapshot(topic: str | None = None) -> dict[str, list[Callable[[Event], Any]]]:
        return DefaultBus.snapshot(topic)
