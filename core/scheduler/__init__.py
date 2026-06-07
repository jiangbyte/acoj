"""
Scheduler module — mirrors hei-gin's ``sdk/scheduler/``.

Provides cron-based background task scheduling that integrates with
the plugin lifecycle (auto-starts on ``on_start()``, auto-stops on ``on_stop()``).

Uses ``SafeCall`` for panic-safe task execution (mirrors hei-gin's
``taskWrapper`` with panic recovery).

Usage::

    from core.scheduler import register_task, register_interval

    register_task("@every 5m", my_task, "health-check")
    register_interval(300, my_task, "cleanup")
"""

from __future__ import annotations

import asyncio
import logging
import threading
import time
from typing import Any, Callable, Optional

from core.middleware import SafeCall

logger = logging.getLogger(__name__)


class ScheduledTask:
    """A single scheduled task with its spec and callable."""

    def __init__(self, spec: str, fn: Callable, name: str = ""):
        self.spec = spec
        self.fn = fn
        self.name = name or fn.__name__
        self._interval_seconds: float = 0.0
        self._parse_spec()

    def _parse_spec(self) -> None:
        spec = self.spec.strip()
        if spec.startswith("@every "):
            part = spec[len("@every "):].strip()
            self._interval_seconds = _parse_duration(part)
        elif spec == "@daily":
            self._interval_seconds = 86400
        elif spec == "@hourly":
            self._interval_seconds = 3600
        elif spec == "@minutely":
            self._interval_seconds = 60
        else:
            try:
                self._interval_seconds = float(spec)
            except ValueError:
                logger.warning("Unsupported schedule spec '%s', defaulting to 60s", spec)
                self._interval_seconds = 60.0

    @property
    def interval_seconds(self) -> float:
        return self._interval_seconds


def _parse_duration(s: str) -> float:
    s = s.strip().lower()
    if s.endswith("h"):
        return float(s[:-1]) * 3600
    elif s.endswith("m"):
        return float(s[:-1]) * 60
    elif s.endswith("s"):
        return float(s[:-1])
    else:
        return float(s)


# ── Registry ─────────────────────────────────────────────────────────

_tasks: list[ScheduledTask] = []
_running = False
_thread: Optional[threading.Thread] = None
_stop_event = threading.Event()


def register_task(spec: str, fn: Callable, name: str = "") -> ScheduledTask:
    """Register a scheduled task.

    Mirrors hei-gin's ``scheduler.Register(spec, task)``.
    """
    task = ScheduledTask(spec, fn, name)
    _tasks.append(task)
    logger.info("[Scheduler] Registered task: %s [spec=%s]", task.name, spec)
    return task


def register_interval(seconds: float, fn: Callable, name: str = "") -> ScheduledTask:
    """Register a task that runs at a fixed interval.

    Mirrors hei-gin's ``scheduler.RegisterInterval(d, task)``.
    """
    return register_task(f"@every {seconds}s", fn, name)


# ── Lifecycle ────────────────────────────────────────────────────────

def start() -> None:
    """Start the scheduler background thread.

    Mirrors hei-gin's ``scheduler.Start()``.
    """
    global _running, _thread, _stop_event

    if _running:
        return

    _running = True
    _stop_event.clear()
    _thread = threading.Thread(target=_run_loop, daemon=True, name="scheduler")
    _thread.start()
    logger.info("[Scheduler] Started with %d task(s)", len(_tasks))


def stop() -> None:
    """Gracefully stop the scheduler.

    Mirrors hei-gin's ``scheduler.Stop()``.
    """
    global _running, _thread

    if not _running:
        return

    _running = False
    _stop_event.set()
    if _thread:
        _thread.join(timeout=10)
        _thread = None
    logger.info("[Scheduler] Stopped")


def _run_loop() -> None:
    """Main scheduler loop — runs each task at its interval with SafeCall."""
    if not _tasks:
        return

    last_run = {id(t): 0.0 for t in _tasks}

    while not _stop_event.is_set():
        now = time.time()
        for task in _tasks:
            task_id = id(task)
            if now - last_run[task_id] >= task.interval_seconds:
                last_run[task_id] = now

                # SafeCall wraps the task with panic recovery
                err = SafeCall(task.fn)
                if err:
                    logger.error("[Scheduler] Task %s failed: %s", task.name, err)
                elif asyncio.iscoroutine(err):
                    logger.warning(
                        "[Scheduler] Task %s returned coroutine — "
                        "scheduled tasks must be sync functions", task.name,
                    )

        _stop_event.wait(timeout=1.0)


__all__ = [
    "ScheduledTask",
    "register_task",
    "register_interval",
    "start",
    "stop",
]
