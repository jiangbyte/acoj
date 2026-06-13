"""
Pluggable log persistence interface — mirrors hei-gin's ``api/log.go`` + ``sdk/log/syslog.go``.

Defines ``LogPersistenceAPI`` and a global ``log_persister`` callback,
making the log storage backend swappable (e.g. DB, Redis, external service).

Usage::

    from sdk.log.persistence import LogPersistenceAPI, set_log_persister

    class MyLogStore(LogPersistenceAPI):
        def save_log(self, entry: LogEntry) -> None:
            db.session.add(LogModel(**entry))

    set_log_persister(MyLogStore())
"""

from __future__ import annotations

import asyncio
import logging
from dataclasses import dataclass
from typing import Callable, Optional, Protocol


@dataclass
class LogEntry:
    """A single operation log entry.

    Mirrors hei-gin's ``api.LogEntry``.
    """
    id: str = ""
    category: str = "OPERATE"        # OPERATE | EXCEPTION | LOGIN | LOGOUT
    name: str = ""
    exe_status: str = "SUCCESS"      # SUCCESS | FAIL
    exe_message: str = ""
    op_ip: str = ""
    op_address: str = ""
    op_browser: str = ""
    op_os: str = ""
    op_user: str = ""
    trace_id: str = ""
    sign_data: str = ""
    req_method: str = ""
    req_url: str = ""
    param_json: str = ""
    result_json: str = ""
    op_time: str = ""
    class_name: str = ""
    method_name: str = ""


class LogPersistenceAPI(Protocol):
    """Interface for log persistence backends.

    Mirrors hei-gin's ``api.LogPersistenceAPI``.
    """

    def save_log(self, entry: LogEntry) -> None:
        """Persist a log entry."""
        ...


# ── Global persister ────────────────────────────────────────────────

_log_persister: Optional[LogPersistenceAPI] = None
_op_user_resolver: Optional[Callable[[str], Optional[str]]] = None
_background_persister: Optional["AsyncQueueLogPersister"] = None
logger = logging.getLogger(__name__)


def set_log_persister(persister: LogPersistenceAPI) -> None:
    """Set the global log persistence backend.

    Mirrors hei-gin's pattern of assigning ``log.LogPersistence = fn``.
    """
    global _log_persister
    _log_persister = persister


def get_log_persister() -> Optional[LogPersistenceAPI]:
    """Return the current log persistence backend."""
    return _log_persister


def set_op_user_resolver(resolver: Callable[[str], Optional[str]]) -> None:
    global _op_user_resolver
    _op_user_resolver = resolver


def get_op_user_resolver() -> Optional[Callable[[str], Optional[str]]]:
    return _op_user_resolver


class AsyncQueueLogPersister:
    def __init__(self, delegate: LogPersistenceAPI, *, max_queue_size: int = 10000, workers: int = 1) -> None:
        self._delegate = delegate
        self._max_queue_size = max(1, max_queue_size)
        self._workers_count = max(1, workers)
        self._queue: asyncio.Queue[LogEntry | None] | None = None
        self._workers: list[asyncio.Task] = []
        self._dropped = 0
        self._started = False

    @property
    def dropped(self) -> int:
        return self._dropped

    def snapshot(self) -> dict[str, int | bool]:
        queue_size = self._queue.qsize() if self._queue is not None else 0
        return {
            "started": self._started,
            "queue_size": queue_size,
            "max_queue_size": self._max_queue_size,
            "workers": self._workers_count,
            "dropped": self._dropped,
        }

    async def start(self) -> None:
        if self._started:
            return
        self._queue = asyncio.Queue(maxsize=self._max_queue_size)
        self._workers = [asyncio.create_task(self._worker()) for _ in range(self._workers_count)]
        self._started = True

    async def stop(self) -> None:
        if not self._started or self._queue is None:
            return
        for _ in self._workers:
            await self._queue.put(None)
        await self._queue.join()
        await asyncio.gather(*self._workers, return_exceptions=True)
        self._workers.clear()
        self._started = False

    def save_log(self, entry: LogEntry) -> None:
        if not self._started or self._queue is None:
            self._delegate.save_log(entry)
            return
        try:
            self._queue.put_nowait(entry)
        except asyncio.QueueFull:
            self._dropped += 1
            logger.warning("log queue full, dropping log entry: category=%s name=%s dropped=%d", entry.category, entry.name, self._dropped)

    async def _worker(self) -> None:
        assert self._queue is not None
        while True:
            entry = await self._queue.get()
            try:
                if entry is None:
                    return
                await asyncio.to_thread(self._delegate.save_log, entry)
            except Exception:
                logger.exception("async log worker failed")
            finally:
                self._queue.task_done()


def configure_async_log_persister(delegate: LogPersistenceAPI, *, max_queue_size: int = 10000, workers: int = 1) -> AsyncQueueLogPersister:
    global _background_persister
    persister = AsyncQueueLogPersister(delegate, max_queue_size=max_queue_size, workers=workers)
    _background_persister = persister
    set_log_persister(persister)
    return persister


async def start_log_persister() -> None:
    if _background_persister is not None:
        await _background_persister.start()


async def stop_log_persister() -> None:
    if _background_persister is not None:
        await _background_persister.stop()


def log_persister_snapshot() -> dict[str, int | bool]:
    if _background_persister is None:
        return {
            "started": False,
            "queue_size": 0,
            "max_queue_size": 0,
            "workers": 0,
            "dropped": 0,
        }
    return _background_persister.snapshot()


def save_log(entry: LogEntry) -> None:
    """Persist a log entry via the configured backend.

    If no backend is configured, the log is written to Python's logger.
    """
    persister = _log_persister
    if persister is not None:
        persister.save_log(entry)
    else:
        import logging
        logging.getLogger(__name__).warning(
            "No log persister configured — dropping log: %s [%s] %s",
            entry.category, entry.exe_status, entry.name,
        )
