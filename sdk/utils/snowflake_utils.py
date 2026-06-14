"""Thread-safe Snowflake ID generation.

``snowflake.SnowflakeGenerator.__next__`` is a plain generator with no
internal locking and may return ``None`` when the per-millisecond sequence
is exhausted or the wall clock moves backwards. Calling it concurrently from
the FastAPI threadpool (sync handlers) could raise
``ValueError: generator already executing`` or, worse, surface ``None`` which
``str(None)`` would turn into the literal id ``"None"``.

This module serialises access with a lock and never returns ``None``.
"""

from __future__ import annotations

import threading
import time

from snowflake import SnowflakeGenerator

from sdk.config.settings import settings

_lock = threading.Lock()
_generator = SnowflakeGenerator(instance=settings.snowflake.instance)

# Bounded retry: a ``None`` from the generator only clears once the wall clock
# advances to the next millisecond, so a short spin is the correct remedy.
_MAX_RETRIES = 100
_RETRY_SLEEP = 0.0005  # 500µs


def configure_instance(instance: int) -> None:
    """Rebuild the generator with a new instance id.

    Used by ``gunicorn.conf.py`` ``post_fork`` so each worker process owns a
    unique Snowflake instance id and cannot collide with its siblings.
    """
    global _generator
    with _lock:
        _generator = SnowflakeGenerator(instance=instance)


def generate_id() -> str:
    with _lock:
        for _ in range(_MAX_RETRIES):
            value = next(_generator)
            if value is not None:
                return str(value)
            time.sleep(_RETRY_SLEEP)
    raise RuntimeError(
        "snowflake id generation failed: clock moved backwards or sequence exhausted"
    )
